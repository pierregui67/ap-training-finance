/*
 * (C) Quartet FS 2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import static org.springframework.security.config.BeanIds.SPRING_SECURITY_FILTER_CHAIN;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration.Dynamic;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.qfs.sandbox.security.impl.CookieUtil;

/**
 * Abstract initializer of the Web Application.
 * <p>
 * When bootstrapped by a servlet-3.0 application container, the Spring Framework will automatically
 * create an instance of this class and call its startup callback method.
 * <p>
 * The content of this class replaces the old web.xml file in previous versions of the servlet
 * specification.
 *
 * @author Quartet FS
 *
 */
public abstract class ASanboxServerWebAppInitializer {

	/**
	 * Configure the given {@link ServletContext} with any servlets, filters, listeners
	 * context-params and attributes necessary for initializing this web application. See examples
	 * {@linkplain WebApplicationInitializer above}.
	 *
	 * @param servletContext the {@code ServletContext} to initialize
	 * @throws ServletException if any call against the given {@code ServletContext} throws a
	 *         {@code ServletException}
	 */
	public void onStartup(ServletContext servletContext) throws ServletException {
		// Spring Context Bootstrapping
		AnnotationConfigWebApplicationContext rootAppContext = new AnnotationConfigWebApplicationContext();
		registerServerConfig(rootAppContext);
		servletContext.addListener(new ContextLoaderListener(rootAppContext));
		registerAdditionalListener(servletContext);

		// Set the session cookie name. Must be done when there are several servers (AP,
		// Content server, ActiveMonitor) with the same URL but running on different ports.
		// Cookies ignore the port (See RFC 6265).
		CookieUtil.configure(servletContext.getSessionCookieConfig(), serverCookieName());

		// The main servlet/the central dispatcher
		final DispatcherServlet servlet = new DispatcherServlet(rootAppContext);
		servlet.setDispatchOptionsRequest(true);
		Dynamic dispatcher = servletContext.addServlet("springDispatcherServlet", servlet);
		dispatcher.addMapping("/*");
		dispatcher.setLoadOnStartup(1);

		// Spring Security Filter
		final FilterRegistration.Dynamic springSecurity = servletContext
				.addFilter(SPRING_SECURITY_FILTER_CHAIN, new DelegatingFilterProxy());
		springSecurity.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");

	}

	/**
	 * No-op method
	 * <p>
	 * Child class who wants to add their own listeners should override this method
	 *
	 * @param servletContext the {@code ServletContext} to initialize
	 */
	protected void registerAdditionalListener(ServletContext servletContext) {/* Do nothing */}

	/**
	 * Register the server's Spring configuration class to the web app
	 *
	 * @param rootAppContext The root context of the web app
	 */
	protected abstract void registerServerConfig(AnnotationConfigWebApplicationContext rootAppContext);

	/**
	 * @return the name for the cookie created by the server
	 */
	protected abstract String serverCookieName();

}
