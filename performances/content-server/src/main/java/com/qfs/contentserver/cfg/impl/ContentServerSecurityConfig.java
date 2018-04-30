/*
 * (C) Quartet FS 2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.contentserver.cfg.impl;

import static com.qfs.content.cfg.impl.ContentServerRestServicesConfig.PING_SUFFIX;
import static com.qfs.content.cfg.impl.ContentServerRestServicesConfig.REST_API_URL_PREFIX;

import com.qfs.sandbox.security.impl.ASecurityConfig;
import com.qfs.sandbox.security.impl.ActiveUISecurityConfigurer;
import com.qfs.sandbox.security.impl.JwtSecurityConfigurer;
import com.qfs.sandbox.security.impl.VersionSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Spring configuration for security on Content server with the fact that ActiveUI is on the same
 * machine as the content server
 *
 * @author Quartet FS
 *
 */
@Import(value={
		ActiveUISecurityConfigurer.class,
		JwtSecurityConfigurer.class,
		VersionSecurityConfigurer.class,
		//InMemoryDownloadLinkConsumerSecurityConfigurer.class,
})
@Configuration
@EnableWebSecurity
public class ContentServerSecurityConfig extends ASecurityConfig {

	/** Cookie name for sessions over ActiveMonitor server */
	public static final String COOKIE_NAME = "CS_JSESSIONID";

	/**
	 * To expose the Content REST service and Ping REST service
	 *
	 * @author Quartet FS
	 *
	 */
	@Configuration
	@Order(5)
	public static class ContentServerSecurityConfigurer extends AWebSecurityConfigurer {

		/** Constructor */
		public ContentServerSecurityConfigurer() {
			super(COOKIE_NAME);
		}

		@Override
		protected void doConfigure(final HttpSecurity http) throws Exception {
			// The order of antMatchers does matter!
			http.authorizeRequests()
					.antMatchers(HttpMethod.OPTIONS, "/**")
					.permitAll()
					// Ping service used by ActiveUI (not protected)
					.antMatchers(REST_API_URL_PREFIX + PING_SUFFIX)
					.permitAll()
					.antMatchers("/**")
					.hasAnyAuthority(ROLE_USER, ROLE_TECH)
					.and()
					.httpBasic();
		}

		@Bean(name = BeanIds.AUTHENTICATION_MANAGER)
		@Override
		public AuthenticationManager authenticationManagerBean() throws Exception {
			return super.authenticationManagerBean();
		}
	}

}
