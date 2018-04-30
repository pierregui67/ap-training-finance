/*
 * (C) Quartet FS 2013-2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;
import java.util.Arrays;
import java.util.logging.Logger;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.qfs.sandbox.cfg.content.impl.EmbeddedContentServiceConfig;
import com.qfs.sandbox.util.impl.ProfilesUtil;

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
public class ActivePivotServerWebAppInitializer
		extends		ASanboxServerWebAppInitializer
		implements	WebApplicationInitializer
{
	/** Our {@link Logger logger} */
	private static final Logger LOGGER = Logger.getLogger(ActivePivotServerWebAppInitializer.class.getName());

	@Override
	protected void registerServerConfig(AnnotationConfigWebApplicationContext rootAppContext) {
		final ConfigurableEnvironment env = rootAppContext.getEnvironment();

		// Set the default profiles if needed
		final String[] profiles = ProfilesUtil.getEnabledProfiles(env);
		if (profiles == null || profiles.length == 0 || (profiles.length == 1 && "default".equals(profiles[0]))) {
			env.addActiveProfile(EmbeddedContentServiceConfig.SPRING_PROFILE);
			//env.addActiveProfile(EmbeddedActiveMonitorConfig.SPRING_PROFILE);
		}

		// Print the active Spring profiles
		LOGGER.info("Running with Spring profile(s): " + Arrays.toString(ProfilesUtil.getEnabledProfiles(env)));

		// Register our Spring config
		rootAppContext.register(ActivePivotServerConfig.class);
	}

	@Override
	protected String serverCookieName() {
		return ActivePivotServerSecurityConfig.AP_COOKIE_NAME;
	}

}