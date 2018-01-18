/*
 * (C) Quartet FS 2013-2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.contributions.impl.ClasspathContributionProvider;

/**
 * Spring configuration of the ActivePivot server.
 *
 * <p>
 * In additional to the generic {@link SandboxConfig}, this configuration specifies that
 * the ActivePivot Server will use a remote Content server and a remote ActiveMonitor
 * server.
 *
 * <p>
 * This is the entry point for the Spring "Java Config" of the entire application. This is
 * referenced in {@link ActivePivotServerWebAppInitializer} to bootstrap the application
 * (as per Spring framework principles).
 *
 * <p>
 * We use {@link PropertySource} annotation(s) to define some .properties file(s), whose content
 * will be loaded into the Spring {@link Environment}, allowing some externally-driven configuration
 * of the application. Parameters can be quickly changed by modifying the
 * {@code content.service.properties} file.
 *
 * <p>
 * We use {@link Import} annotation(s) to reference additional Spring {@link Configuration} classes,
 * so that we can manage the application configuration in a modular way (split by domain/feature,
 * re-use of core config, override of core config, customized config, etc...).
 *
 * <p>
 * Spring best practices recommends not to have arguments in bean methods if possible. One should
 * rather autowire the appropriate spring configurations (and not beans directly unless necessary),
 * and use the beans from there.
 *
 *
 * @author Quartet FS
 */
@PropertySource(
		value = {
				"classpath:content.service.properties",
				"classpath:jwt.properties" })
@Configuration
@Import(value = {
		SandboxConfig.class,
		ActivePivotServerSecurityConfig.class,

		// We have the remote content service config here
		LocalContentServiceConfig.class
})
public class ActivePivotServerConfig {

	/** Before anything else we statically initialize the Quartet FS Registry. */
	static {
		Registry.setContributionProvider(new ClasspathContributionProvider("com.qfs", "com.quartetfs"));
	}

}
