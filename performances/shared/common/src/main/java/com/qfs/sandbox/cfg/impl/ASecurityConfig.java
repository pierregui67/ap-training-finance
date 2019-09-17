/*
 * (C) Quartet FS 2015-2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import java.util.Arrays;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import com.qfs.content.service.IContentService;
import com.qfs.jwt.impl.JwtFilter;
import com.qfs.jwt.service.IJwtService;
import com.qfs.sandbox.security.impl.InMemoryUserDetailsManagerBuilder;
import com.qfs.security.cfg.ICorsFilterConfig;
import com.qfs.security.spring.impl.CompositeUserDetailsService;
import com.qfs.server.cfg.IJwtConfig;
import com.qfs.server.cfg.impl.JwtRestServiceConfig;
import com.qfs.server.cfg.impl.VersionServicesConfig;
import com.qfs.servlet.handlers.impl.NoRedirectLogoutSuccessHandler;
import com.quartetfs.biz.pivot.security.IAuthorityComparator;
import com.quartetfs.biz.pivot.security.impl.AuthorityComparatorAdapter;
import com.quartetfs.fwk.ordering.impl.CustomComparator;

/**
 * Generic implementation for security configuration of a server hosting ActivePivot, or Content
 * server or ActiveMonitor.
 * <p>
 * This class contains methods:
 * <ul>
 * <li>To define authorized users</li>,
 * <li>To enable anomymous user access</li>,
 * <li>To configure the JWT filter</li>,
 * <li>To configure the security for Version service</li>.
 * </ul>
 *
 * @author Quartet FS
 */
@EnableGlobalAuthentication
@Configuration
public class ASecurityConfig {

	/** Set to true to allow anonymous access */
	public static final boolean useAnonymous = false;

	public static final String ROLE_USER = "ROLE_USER";
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	public static final String ROLE_TECH = "ROLE_TECH";
	public static final String ROLE_CS_ROOT = IContentService.ROLE_ROOT;
	public static final String COOKIE_NAME = "AP_JSESSIONID";

	/**
	 * ROLE_DESK_A is added to users, to give them permission
	 * to read kpis created by other users in the content server
	 * In order to "share" kpis created in the content server, the kpi
	 * reader role is set to : ROLE_DESK_A
	 */
	public static final String ROLE_KPI = "ROLE_KPI";

	public static final String PIVOT_USER = "pivot";
	public static final String[] PIVOT_USER_ROLES = { ROLE_TECH, ROLE_CS_ROOT };

	protected static final AuthenticationEntryPoint authenticationEntryPoint = new HttpStatusEntryPoint(
			HttpStatus.UNAUTHORIZED);

	@Autowired
	protected Environment env;

	@Autowired
	protected IJwtConfig jwtConfig;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth
				.eraseCredentials(false)
				// Add an LDAP authentication provider instead of this to support LDAP
				.userDetailsService(userDetailsService()).and()
				// Required to allow JWT
				.authenticationProvider(jwtConfig.jwtAuthenticationProvider());
	}

	/**
	 * [Bean] Create the users that can access the server
	 *
	 * @return {@link UserDetailsService user data}
	 */
	@Bean
	public UserDetailsService userDetailsService() {
		InMemoryUserDetailsManagerBuilder b = new InMemoryUserDetailsManagerBuilder()
				.withUser("admin").password("admin").authorities(ROLE_USER, ROLE_ADMIN, ROLE_KPI, ROLE_CS_ROOT).and()
				.withUser("user1").password("user1").authorities(ROLE_USER, ROLE_KPI, "ROLE_DESK_A").and()
				.withUser("user2").password("user2").authorities(ROLE_USER, "ROLE_EUR_USD").and()
				.withUser("manager1").password("manager1").authorities(ROLE_USER, ROLE_KPI).and()
				.withUser("manager2").password("manager2").authorities(ROLE_USER, ROLE_KPI).and();

		return new CompositeUserDetailsService(Arrays.asList(b.build(),
				technicalUserDetailsService()));
	}

	/**
	 * [Bean] Create the technical users (one for ActiveUI, one for ActivePivot) that can be used to
	 * authenticate the connection from ActiveUI or ActivePivot
	 *
	 * @return {@link UserDetailsService user data}
	 */
	protected UserDetailsManager technicalUserDetailsService() {
		return new InMemoryUserDetailsManagerBuilder()
				// Technical user for ActivePivot Live access
				.withUser("live").password("live").authorities(ROLE_TECH).and()
				// Technical user for ActivePivot server
				.withUser(PIVOT_USER).password("pivot").authorities(PIVOT_USER_ROLES).and()
				.build();
	}

	/**
	 * [Bean] {@link JwtFilter JWT filter} for the server
	 *
	 * @param authenticationManagerBean the bean for the filter to submit authentication requests to
	 * @return the JWT filter
	 */
	@Bean
	public JwtFilter jwtFilter(AuthenticationManager authenticationManagerBean) {
		return new JwtFilter(authenticationManagerBean);
	}

	/**
	 * [Bean] Comparator for user roles
	 * <p>
	 * Defines the comparator used by:
	 * </p>
	 * <ul>
	 *   <li>com.quartetfs.biz.pivot.security.impl.ContextValueManager#setAuthorityComparator(IAuthorityComparator)</li>
	 *   <li>{@link IJwtService}</li>
	 * </ul>
	 * @return a comparator that indicates which authority/role prevails over another. <b>NOTICE -
	 *         an authority coming AFTER another one prevails over this "previous" authority.</b>
	 *         This authority ordering definition is essential to resolve possible ambiguity when,
	 *         for a given user, a context value has been defined in more than one authority
	 *         applicable to that user. In such case, it is what has been set for the "prevailing"
	 *         authority that will be effectively retained for that context value for that user.
	 */
	@Bean
	public IAuthorityComparator authorityComparator() {
		final CustomComparator<String> comp = new CustomComparator<>();
		comp.setFirstObjects(Arrays.asList(ROLE_USER));
		comp.setLastObjects(Arrays.asList(ROLE_ADMIN));
		return new AuthorityComparatorAdapter(comp);
	}

	/**
	 * Common configuration for {@link HttpSecurity}.
	 *
	 * @author Quartet FS
	 */
	public abstract static class AWebSecurityConfigurer extends WebSecurityConfigurerAdapter {

		/** {@code true} to enable the logout URL */
		protected final boolean logout;
		/** The name of the cookie to clear */
		protected final String cookieName;

		@Autowired
		protected ApplicationContext context;

		/**
		 * This constructor does not enable the logout URL
		 */
		public AWebSecurityConfigurer() {
			this(null);
		}

		/**
		 * This constructor enables the logout URL
		 *
		 * @param cookieName the name of the cookie to clear
		 */
		public AWebSecurityConfigurer(String cookieName) {
			this.logout = cookieName != null;
			this.cookieName = cookieName;
		}

		@Override
		protected final void configure(final HttpSecurity http) throws Exception {
			JwtFilter jwtFilter = context.getBean(JwtFilter.class);
			Filter corsFilter = context.getBean(ICorsFilterConfig.class).corsFilter();

			http
					// As of Spring Security 4.0, CSRF protection is enabled by default.
					.csrf().disable()
					// Configure CORS
					.addFilterBefore(corsFilter, SecurityContextPersistenceFilter.class)
					// To allow authentication with JWT (Required for ActiveUI)
					.addFilterAfter(jwtFilter, SecurityContextPersistenceFilter.class);

			if (logout) {
				// Configure logout URL
				http.logout()
						.permitAll()
						.deleteCookies(cookieName)
						.invalidateHttpSession(true)
						.logoutSuccessHandler(new NoRedirectLogoutSuccessHandler());
			}

			if (useAnonymous) {
				// Handle anonymous users. The granted authority ROLE_USER
				// will be assigned to the anonymous request
				http.anonymous().principal("guest").authorities(ROLE_USER);
			}

			doConfigure(http);
		}

		/**
		 * @see #configure(HttpSecurity)
		 */
		protected abstract void doConfigure(HttpSecurity http) throws Exception;

	}

	/**
	 * Configuration for JWT.
	 * <p>
	 * The most important point is the {@code authenticationEntryPoint}. It must
	 * only send an unauthorized status code so that JavaScript clients can
	 * authenticate (otherwise the browser will intercepts the response).
	 *
	 * @author Quartet FS
	 * @see HttpStatusEntryPoint
	 */
	public abstract static class AJwtSecurityConfigurer extends WebSecurityConfigurerAdapter {

		@Autowired
		protected ApplicationContext context;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			Filter corsFilter = context.getBean(ICorsFilterConfig.class).corsFilter();

			http
					.antMatcher(JwtRestServiceConfig.REST_API_URL_PREFIX + "/**")
					// As of Spring Security 4.0, CSRF protection is enabled by default.
					.csrf().disable()
					// Configure CORS
					.addFilterBefore(corsFilter, SecurityContextPersistenceFilter.class)
					.authorizeRequests()
					.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
					.antMatchers("/**").hasAnyAuthority(ROLE_USER)
					.and()
					.httpBasic().authenticationEntryPoint(authenticationEntryPoint);
		}

	}

	/**
	 * Configuration for Version service to allow anyone to access this service
	 *
	 * @author Quartet FS
	 * @see HttpStatusEntryPoint
	 */
	public abstract static class AVersionSecurityConfigurer extends WebSecurityConfigurerAdapter {

		@Autowired
		protected ApplicationContext context;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			Filter corsFilter = context.getBean(ICorsFilterConfig.class).corsFilter();

			http
					.antMatcher(VersionServicesConfig.REST_API_URL_PREFIX + "/**")
					// As of Spring Security 4.0, CSRF protection is enabled by default.
					.csrf().disable()
					// Configure CORS
					.addFilterBefore(corsFilter, SecurityContextPersistenceFilter.class)
					.authorizeRequests()
					.antMatchers("/**").permitAll();
		}

	}

	/**
	 * Configuration for ActiveUI.
	 *
	 * @author Quartet FS
	 * @see HttpStatusEntryPoint
	 */
	public abstract static class AActiveUISecurityConfigurer extends AWebSecurityConfigurer {

		@Override
		protected void doConfigure(HttpSecurity http) throws Exception {
			// Permit all on ActiveUI resources and the root (/) that redirects to ActiveUI index.html.
			final String pattern = "^(.{0}|\\/|\\/" + ActiveUIResourceServerConfig.NAMESPACE + "(\\/.*)?)$";
			http
					// Only theses URLs must be handled by this HttpSecurity
					.regexMatcher(pattern)
					.authorizeRequests()
					// The order of the matchers matters
					.regexMatchers(HttpMethod.OPTIONS, pattern)
					.permitAll()
					.regexMatchers(HttpMethod.GET, pattern)
					.permitAll();

			// Authorizing pages to be embedded in iframes to have ActiveUI in ActiveMonitor UI
			http.headers().frameOptions().disable();
		}

	}

}
