/*
 * (C) Quartet FS 2013-2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import com.qfs.distribution.security.IDistributedSecurityManager;
import com.qfs.messenger.IDistributedMessenger;
import com.qfs.monitoring.HealthCheckAgent;
import com.qfs.pivot.content.impl.DynamicActivePivotContentServiceMBean;
import com.qfs.sandbox.bean.DatastoreConfigBean;
import com.qfs.sandbox.postprocessor.impl.ForexDisplayHandler;
import com.qfs.sandbox.postprocessor.impl.ForexHandler;
import com.qfs.sandbox.postprocessor.impl.ForexStream;
import com.qfs.server.cfg.IActivePivotConfig;
//import com.qfs.server.cfg.IActivePivotContentServiceConfig;
import com.qfs.server.cfg.IDatastoreConfig;
import com.qfs.server.cfg.IJwtConfig;
import com.qfs.server.cfg.content.IActivePivotContentServiceConfig;
import com.qfs.server.cfg.impl.*;
//import com.quartetfs.biz.pivot.monitoring.impl.JMXEnabler;
import com.quartetfs.biz.pivot.monitoring.impl.DynamicActivePivotManagerMBean;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousHandler;
import com.quartetfs.biz.pivot.query.aggregates.IStream;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.monitoring.jmx.impl.JMXEnabler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import static com.quartetfs.fwk.types.impl.ExtendedPluginInjector.inject;

/**
 * Generic Spring configuration of the Sandbox ActivePivot server application.
 *
 * <p>
 * This is the entry point for the Spring "Java Config" of the entire application. This is
 * referenced in corresponding WebAppInitializer to bootstrap the application (as per Spring
 * framework principles).
 *
 * <p>
 * We use {@link PropertySource} annotation(s) to define some .properties file(s), whose content
 * will be loaded into the Spring {@link Environment}, allowing some externally-driven configuration
 * of the application.
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
 * @author Quartet FS
 */
@PropertySource(value = { "classpath:jwt.properties" })
@Configuration
@Import(
value = {
		ActivePivotConfig.class,
		DatastoreConfig.class,
		SourceConfig.class,
		JwtConfig.class,
		SandboxCorsFilterConfig.class,

		ActivePivotServicesConfig.class,
		ActivePivotWebServicesConfig.class,
		ActivePivotRemotingServicesConfig.class,
		ActivePivotXmlaServletConfig.class,

		//QfsRestServicesConfig.class,

		ActivePivotRestServicesConfig.class,

		// Streaming Services monitor
		StreamingMonitorConfig.class,

        DatastoreConfigBean.class
})
public class SandboxConfig {

	/** Spring environment, automatically wired */
	@Autowired
	protected Environment env;

	/** Datastore spring configuration */
	@Autowired
	protected IDatastoreConfig datastoreConfig;

	/** ActivePivot spring configuration */
	@Autowired
	protected IActivePivotConfig apConfig;

	/** JWT spring configuration */
	@Autowired
	protected IJwtConfig jwtConfig;

	/** ActivePivot content service spring configuration */
	@Autowired
	protected IActivePivotContentServiceConfig apCSConfig;

	/** SecurityConfig spring configuration */
	@Autowired
	protected ActivePivotServerSecurityConfig securityConfig;

	/** Repository extension for ActivePivot configuration */

	/** ActivePivot Service Config */
	@Autowired
	protected ActivePivotServicesConfig apServiceConfig;

	@Autowired
    protected DatastoreConfigBean datastoreConfigBean;

	/**
	 *
	 * Initialize and start the ActivePivot Manager, after performing all the injections into the
	 * ActivePivot plug-ins.
	 *
	 * @see #apManagerInitPrerequisitePluginInjections()
	 * @return void
	 * @throws Exception any exception that occurred during the manager's start up
	 */
	@Bean
	public Void startManager() throws Exception {
		/* ********************************************************************** */
		/* Inject dependencies before the ActivePivot components are initialized. */
		/* ********************************************************************** */
		apManagerInitPrerequisitePluginInjections();

		/* *********************************************** */
		/* Initialize the ActivePivot Manager and start it */
		/* *********************************************** */

		apConfig.activePivotManager().init(null);
		apConfig.activePivotManager().start();

		return null;
	}

	/**
	 * Enable JMX Monitoring for the Datastore
	 *
	 * @return the {@link JMXEnabler} attached to the datastore
	 */
	@Bean
	public JMXEnabler JMXDatastoreEnabler() {
		return new JMXEnabler(datastoreConfig.datastore());
	}

	/**
	 * Enable JMX Monitoring for ActivePivot Components
	 *
	 * @return the {@link JMXEnabler} attached to the activePivotManager
	 */
	@Bean
	@DependsOn(value = "startManager")
	public JMXEnabler JMXActivePivotEnabler() {
		return new JMXEnabler(new DynamicActivePivotManagerMBean(apConfig.activePivotManager()));
	}

	/**
	 * Enable JMX Monitoring for the Content Service
	 *
	 * @return the {@link JMXEnabler} attached to the content service.
	 */
	@Bean
	public JMXEnabler JMXActivePivotContentServiceEnabler() {
		// to allow operations from the JMX bean
		return new JMXEnabler(
				new DynamicActivePivotContentServiceMBean(
						apCSConfig.activePivotContentService(),
						apConfig.activePivotManager()));
	}

	/**
	 * Health Check Agent bean
	 *
	 * @return the health check agent
	 */
	@Bean(initMethod = "start", destroyMethod = "interrupt")
	public HealthCheckAgent healthCheckAgent() {
		return new HealthCheckAgent(60); // One trace per minute
	}

	/**
	 * Extended plugin injections that are required before doing the startup of the ActivePivot
	 * manager.
	 *
	 * @see #startManager()
	 * @throws Exception any exception that occurred during the injection
	 */
	protected void apManagerInitPrerequisitePluginInjections() throws Exception {
		/* ********************************************************* */
		/* Core injections for distributed architecture (when used). */
		/* ********************************************************* */
		// Inject the distributed messenger with security services
		for (Object key : Registry.getExtendedPlugin(IDistributedMessenger.class).keys()) {
			inject(IDistributedMessenger.class, String.valueOf(key), apConfig.contextValueManager());
		}

		// Inject the distributed security manager with security services
		for (Object key : Registry.getExtendedPlugin(IDistributedSecurityManager.class).keys()) {
			inject(IDistributedSecurityManager.class, String.valueOf(key), securityConfig.qfsUserDetailsService());
		}

		// Custom injection
        inject(IStream.class, ForexStream.PLUGIN_KEY, "datastore", datastoreConfig.datastore());
        inject(IAggregatesContinuousHandler.class, ForexDisplayHandler.PLUGIN_KEY, "currencyLevel", "CurrencyContextValue");
        inject(IAggregatesContinuousHandler.class, ForexHandler.PLUGIN_KEY, "currencyLevel", "CurrencyContextValue");

        datastoreConfigBean.setDatastoreConfig(datastoreConfig);
	}

}
