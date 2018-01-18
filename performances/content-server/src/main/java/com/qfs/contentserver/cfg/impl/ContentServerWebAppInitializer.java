/*
 * (C) Quartet FS 2014-2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.contentserver.cfg.impl;

import javax.servlet.ServletContext;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.qfs.sandbox.cfg.impl.ASanboxServerWebAppInitializer;
import com.qfs.servlet.impl.SqlDriverCleaner;

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
public class ContentServerWebAppInitializer extends ASanboxServerWebAppInitializer
		implements
		WebApplicationInitializer {

	@Override
	protected void registerServerConfig(AnnotationConfigWebApplicationContext rootAppContext) {
		rootAppContext.register(ContentServerConfig.class);
	}

	@Override
	protected void registerAdditionalListener(ServletContext servletContext) {
		servletContext.addListener(new SqlDriverCleaner());
	}

	@Override
	protected String serverCookieName() {
		return ContentServerSecurityConfig.COOKIE_NAME;
	}
}
