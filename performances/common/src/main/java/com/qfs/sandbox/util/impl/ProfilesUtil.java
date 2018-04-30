/*
 * (C) Quartet FS 2017
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.util.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

/**
 * A utility class that exposes methods used to deal with
 * Spring {@link Profile profiles}.
 *
 * @author ActiveViam
 */
public class ProfilesUtil {

	/**
	 * Private constructor to avoid instantiation.
	 */
	private ProfilesUtil() {}

	/**
	 * Returns whether the profile with the given name is currently enabled.
	 *
	 * @param env The Spring environment.
	 * @param profileName The name of the profile to check.
	 * @return <code>true</code> if the input profile is enabled,
	 *         <code>false</code> otherwise.
	 */
	public static boolean isProfileEnabled(final Environment env, final String profileName) {
		final String[] enabledProfiles = getEnabledProfiles(env);
		if (enabledProfiles != null) {
			for (final String p: enabledProfiles) {
				if (p != null && p.equals(profileName)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the names of the profiles that are currently enabled.
	 *
	 * @param env The Spring environment.
	 * @return The names of the profiles that are currently enabled.
	 */
	public static String[] getEnabledProfiles(final Environment env) {
		final String[] activeProfiles = env.getActiveProfiles();
		if (activeProfiles == null || activeProfiles.length == 0) {
			// No active profiles. Default ones are therefore enabled.
			return env.getDefaultProfiles();
		} else {
			return activeProfiles;
		}
	}


}
