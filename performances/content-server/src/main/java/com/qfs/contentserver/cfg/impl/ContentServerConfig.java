/*
 * (C) Quartet FS 2014-2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.contentserver.cfg.impl;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.bus.spring.SpringBus;
import org.hibernate.cfg.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.qfs.content.cfg.IContentServiceConfig;
import com.qfs.content.cfg.impl.StandaloneContentServerRestConfig;
import com.qfs.content.service.IContentService;
import com.qfs.content.service.audit.impl.AuditableHibernateContentService;
import com.qfs.sandbox.cfg.impl.ActiveUIResourceServerConfig;
import com.qfs.sandbox.cfg.impl.ContentServerUtil;
import com.qfs.sandbox.cfg.impl.SandboxCorsFilterConfig;
import com.qfs.server.cfg.impl.JwtConfig;
import com.quartetfs.fwk.QuartetRuntimeException;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.contributions.impl.ClasspathContributionProvider;

/**
 * Spring configuration for Content server
 *
 * @author Quartet FS
 *
 */
@PropertySource(value = "classpath:jwt.properties")
@Import(value = {
		ActiveUIResourceServerConfig.class,

		StandaloneContentServerRestConfig.class,
		ContentServerSecurityConfig.class,
		SandboxCorsFilterConfig.class,
		JwtConfig.class
})
@org.springframework.context.annotation.Configuration
public class ContentServerConfig implements IContentServiceConfig {

	/** Before anything else we statically initialize the Quartet FS Registry. */
	static {
		Registry.setContributionProvider(new ClasspathContributionProvider("com.qfs", "com.quartetfs"));
	}

	/**
	 * The content service is a bean which can be used by ActivePivot server to store:
	 * <ul>
	 * <li>calculated members and share them between users</li>
	 * <li>the cube descriptions</li>
	 * <li>entitlements</li>
	 * </ul>
	 * @return the content service
	 */
	@Override
	@Bean
	public IContentService contentService() {
		Configuration conf  = ContentServerUtil.loadConfiguration("hibernate.properties");
		return new AuditableHibernateContentService(conf);
	}

	/**
	 * Configure the CXF bus
	 * @return the CXF bus
	 */
	@Bean(destroyMethod = "shutdown")
	public SpringBus cxf() {
		final Bus bus = BusFactory.getDefaultBus(false);
		if (null == bus)
			return new SpringBus();

		throw new QuartetRuntimeException(
				"A bus of type '"
						+ bus.getClass().getName()
						+ ", has already been set as the default CXF bus. This may prevent the rest services to work"
						+ " To solve this issue you may just need to ensure that this bean is created first.");
	}

}
