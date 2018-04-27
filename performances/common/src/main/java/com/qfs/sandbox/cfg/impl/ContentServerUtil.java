/*
 * (C) Quartet FS 2016
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg.impl;

import java.lang.reflect.Method;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.BeanInitializationException;

import com.qfs.content.service.impl.HibernateContentService;
import com.qfs.util.impl.QfsProperties;

/**
 * Base class to share common configurations between local
 * and remote content sever.
 *
 * @author Quartet FS
 */
public class ContentServerUtil {

	/**
	 * Loads the Hibernate's configuration from the specified file.
	 *
	 * @param fileName The name of the file containing the Hibernate's properties
	 * @return the Hibernate's configuration
	 */
	public static Configuration loadConfiguration(String fileName) {
		final Properties hibernateProperties = QfsProperties.loadProperties(fileName);
		hibernateProperties.put(AvailableSettings.DATASOURCE,
				ContentServerUtil.createTomcatJdbcDataSource(hibernateProperties));
		return new Configuration().addProperties(hibernateProperties);
	}

	/**
	 * This {@link DataSource} is specific to the connection pool we want to use with Hibernate.
	 * If you don't want to use the same as we do, you don't need it.
	 *
	 * @param hibernateProperties the hibernate properties loaded from <i>hibernate.properties</i>
	 * file.
	 * @return the {@link DataSource} for {@link HibernateContentService}.
	 */
	public static DataSource createTomcatJdbcDataSource(Properties hibernateProperties) {
		try {
			// Reflection is used to not make the sandbox depends on tomcat-jdbc.jar
			Class<?> dataSourceKlass = Class.forName("org.apache.tomcat.jdbc.pool.DataSourceFactory");
			Method createDataSourceMethod = dataSourceKlass.getMethod("createDataSource", Properties.class);
			return (DataSource) createDataSourceMethod.invoke(dataSourceKlass.newInstance(), hibernateProperties);
		} catch (Exception e) {
			throw new BeanInitializationException("Initialization of " + DataSource.class + " failed", e);
		}
	}

}
