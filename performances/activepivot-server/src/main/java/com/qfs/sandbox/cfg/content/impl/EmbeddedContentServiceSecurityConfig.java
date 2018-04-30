/*
 * (C) Quartet FS 2017
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.content.impl;

import com.qfs.QfsWebUtils;
import com.qfs.content.cfg.impl.ContentServerRestServicesConfig;
import com.qfs.pivot.servlet.impl.ContextValueFilter;
import com.qfs.sandbox.cfg.impl.ActivePivotServerSecurityConfig;
import com.qfs.sandbox.security.impl.ASecurityConfig;
import com.qfs.sandbox.security.impl.ActiveUISecurityConfigurer;
import com.qfs.server.cfg.content.IActivePivotContentServiceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Since the embedded Content Service profile exposes a local
 * ActiveUI, the relevant security configuration needs to be
 * imported in that case.
 *
 * @author ActiveViam
 */
@Import({
	ActiveUISecurityConfigurer.class,
	EmbeddedContentServiceSecurityConfig.ContentServerSecurityConfigurer.class
})
@Profile({EmbeddedContentServiceConfig.SPRING_PROFILE})
@Configuration
public class EmbeddedContentServiceSecurityConfig {

	/**
	 * Only required if the content service is exposed.
	 * <p>
	 * Separated from {@link ActivePivotServerSecurityConfig.ActivePivotSecurityConfigurer} to skip the {@link ContextValueFilter}.
	 * <p>
	 * Must be done before ActivePivotSecurityConfigurer (because they match common URLs)
	 *
	 * @see IActivePivotContentServiceConfig
	 */
	@Configuration
	@Order(5)
	public static class ContentServerSecurityConfigurer extends ASecurityConfig.AWebSecurityConfigurer {

		@Override
		protected void doConfigure(HttpSecurity http) throws Exception {
			final String url = ContentServerRestServicesConfig.NAMESPACE;
			http
				// Only theses URLs must be handled by this HttpSecurity
				.antMatcher(url + "/**")
				.authorizeRequests()
				// The order of the matchers matters
				.antMatchers(
					HttpMethod.OPTIONS,
					QfsWebUtils.url(ContentServerRestServicesConfig.REST_API_URL_PREFIX + "**"))
				.permitAll()
				.antMatchers(url + "/**")
				.hasAuthority(ASecurityConfig.ROLE_USER)
				.and()
				.httpBasic();
		}

	}}
