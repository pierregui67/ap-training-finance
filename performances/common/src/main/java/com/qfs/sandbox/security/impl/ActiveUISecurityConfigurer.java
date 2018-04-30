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
 * To expose the login page of ActiveUI.
 *
 * @author ActiveViam
 */
@Configuration
@Order(1)
public class ActiveUISecurityConfigurer extends ASecurityConfig.AActiveUISecurityConfigurer {}
