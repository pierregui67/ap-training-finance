/*
 * (C) Quartet FS 2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.contentserver.cfg.impl;

import static com.qfs.content.cfg.impl.ContentServerRestServicesConfig.PING_SUFFIX;
import static com.qfs.content.cfg.impl.ContentServerRestServicesConfig.REST_API_URL_PREFIX;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.qfs.sandbox.cfg.impl.ASecurityConfig;

/**
 * Spring configuration for security on Content server with the fact that ActiveUI is on the same
 * machine as the content server
 *
 * @author Quartet FS
 *
 */
@Configuration
@EnableWebSecurity
public class ContentServerSecurityConfig extends ASecurityConfig {

	/**
	 * To expose the login page of ActiveUI.
	 */
	@Configuration
	@Order(1)
	public static class ActiveUISecurityConfigurer extends AActiveUISecurityConfigurer {}

	/**
	 * To expose the JWT REST service
	 * <p>
	 * Must be done before ContentServerSecurityConfigurer (because they match common URLs)
	 *
	 * @author Quartet FS
	 *
	 */
	@Configuration
	@Order(2)
	public static class JwtSecurityConfigurer extends AJwtSecurityConfigurer {}

	/**
	 * To expose the Version REST service
	 *
	 * @author Quartet FS
	 *
	 */
	@Configuration
	@Order(3)
	public static class VersionSecurityConfig extends AVersionSecurityConfigurer {}

	/**
	 * To expose the Content REST service and Ping REST service
	 *
	 * @author Quartet FS
	 *
	 */
	@Configuration
	@Order(4)
	public static class ContentServerSecurityConfigurer extends AWebSecurityConfigurer {

		/** Constructor */
		public ContentServerSecurityConfigurer() {
			super(COOKIE_NAME);
		}

		@Override
		protected void doConfigure(HttpSecurity http) throws Exception {
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
