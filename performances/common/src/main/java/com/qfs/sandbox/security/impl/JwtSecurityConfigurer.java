/*
 * (C) Quartet FS 2017
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.security.impl;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * To expose the JWT REST service
 *
 * @author ActiveViam
 */
@Configuration
@Order(2) // Must be done before ContentServerSecurityConfigurer (because they match common URLs)
public class JwtSecurityConfigurer extends ASecurityConfig.AJwtSecurityConfigurer {}
