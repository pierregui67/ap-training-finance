/*
 * (C) Quartet FS 2013-2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 *
 * Initializer of the Web Application.
 * <p>
 * When bootstrapped by a servlet-3.0 application container, the Spring
 * Framework will automatically create an instance of this class and call its
 * startup callback method.
 * <p>
 * The content of this class replaces the old web.xml file in previous versions
 * of the servlet specification.
 *
 * @author Quartet FS
 *
 */
public class ActivePivotServerWebAppInitializer extends ASanboxServerWebAppInitializer
		implements
		WebApplicationInitializer {

	@Override
	protected void registerServerConfig(AnnotationConfigWebApplicationContext rootAppContext) {
		rootAppContext.register(ActivePivotServerConfig.class);
	}

	@Override
	protected String serverCookieName() {
		return ActivePivotServerSecurityConfig.COOKIE_NAME;
	}

}
