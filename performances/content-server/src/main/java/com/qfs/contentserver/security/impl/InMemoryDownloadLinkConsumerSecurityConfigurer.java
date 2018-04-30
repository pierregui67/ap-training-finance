/*
 * (C) Quartet FS 2017
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
/*package com.qfs.contentserver.security.impl;

import com.activeviam.reporting.plugins.consumers.downloadlink.inmemory.rest.cfg.InMemoryDownloadLinkConsumerRestServicesConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Make sure the {@link InMemoryDownloadLinkConsumerRestServicesConfig} REST API is available without authentication.
 *
@Configuration
@Order(4)
public class InMemoryDownloadLinkConsumerSecurityConfigurer extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		http.antMatcher(InMemoryDownloadLinkConsumerRestServicesConfig.REST_API_URL_PREFIX + "/**")
				.authorizeRequests()
				.antMatchers("/**").permitAll();
	}
}
*/