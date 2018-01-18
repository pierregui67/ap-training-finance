/*
 * (C) Quartet FS 2015-2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import com.qfs.content.cfg.impl.ContentServerRestServicesConfig;
import com.qfs.content.service.IContentService;
import com.qfs.pivot.content.IActivePivotContentService;
import com.qfs.pivot.content.impl.ActivePivotContentServiceBuilder;
import com.qfs.server.cfg.IActivePivotContentServiceConfig;
import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.definitions.ICalculatedMemberDescription;
import com.quartetfs.biz.pivot.definitions.IKpiDescription;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration of the <b>Content Service</b> backed by a local <b>Content Server</b>.
 * <p>
 * This configuration imports {@link ContentServerRestServicesConfig} to expose the content service.
 *
 * @author Quartet FS
 */
@Configuration
public class LocalContentServiceConfig extends AContentServiceConfig implements IActivePivotContentServiceConfig {

	/**
	 * Service used to store the ActivePivot descriptions and the entitlements (i.e.
	 * {@link IContextValue context values}, {@link ICalculatedMemberDescription calculated members}
	 * and {@link IKpiDescription KPIs}).
	 *
	 * @return the {@link IActivePivotContentService content service} used by the Sandbox
	 *         application
	 */
	@Bean
	@Override
	public IActivePivotContentService activePivotContentService() {
		org.hibernate.cfg.Configuration conf  = ContentServerUtil.loadConfiguration("hibernate.properties");
		return new ActivePivotContentServiceBuilder()
				.withPersistence(conf)
				.withAudit()
				.withCacheForEntitlements(-1)

				// WARNING: In production, you should not keep the next lines, which will erase
				// parts of your remote configuration. Prefer pushing them manually using the
				// PushToContentServer utility class before starting the ActivePivot server.

				// Setup directories and permissions
				.needInitialization(
						getEnvironment().getRequiredProperty(CALCULATED_MEMBER_ROLE_PROPERTY),
						getEnvironment().getRequiredProperty(KPI_ROLE_PROPERTY))
				// Push the manager description from DESC-INF
				.withXmlDescription()
				// Push the context values stored in ROLE-INF
//				.withContextValues("ROLE-INF")
				.build();
	}

	@Bean
	@Override
	public IContentService contentService() {
		// Return the real content service used by the activePivotContentService instead of the wrapped one
		return activePivotContentService().getContentService().getUnderlying();
	}

}
