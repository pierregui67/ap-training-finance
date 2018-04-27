/*
 * (C) Quartet FS 2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import com.qfs.server.cfg.content.IActivePivotContentServiceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.qfs.content.service.IContentService;
import com.qfs.pivot.content.IActivePivotContentService;
//import com.qfs.server.cfg.IActivePivotContentServiceConfig;

/**
 * Generic spring configuration for Content service
 *
 * @author Quartet FS
 *
 */
public abstract class AContentServiceConfig implements IActivePivotContentServiceConfig {

	/**
	 * The name of the property which contains the role allowed to add new calculated members in the
	 * configuration service.
	 */
	public static final String CALCULATED_MEMBER_ROLE_PROPERTY = "contentServer.security.calculatedMemberRole";

	/**
	 * The name of the property which contains the role allowed to add new KPIs in the configuration
	 * service.
	 */
	public static final String KPI_ROLE_PROPERTY = "contentServer.security.kpiRole";

	/**
	 * The name of the property which contains Time to Live (TTL) of entitlements.
	 */
	public static final String ENTITLEMENTS_TTL = "contentServer.security.cache.entitlementsTTL";

	/**
	 * The name of the property which contains the URL of the remote Content server
	 */
	public static final String REMOTE_API_URL_PROPERTY = "contentServer.remote.api.uri";

	/**
	 * The Spring environment of the Content service
	 */
	@Autowired
	public Environment env;

	@Bean
	@Override
	public abstract IActivePivotContentService activePivotContentService();

	@Bean
	@Override
	public abstract IContentService contentService();

	/**
	 * @return Spring environment of the Content service
	 */
	protected Environment getEnvironment() {
		return env;
	}

}
