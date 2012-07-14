/*******************************************************************************
 * Copyright (c) 2006-2012
 * Software Technology Group, Dresden University of Technology
 * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Software Technology Group - TU Dresden, Germany;
 *   DevBoost GmbH - Berlin, Germany
 *      - initial API and implementation
 ******************************************************************************/
package de.devboost.eclipse.jloop.launch;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import de.devboost.eclipse.jloop.JLoopPlugin;

public class LauchConfigurationHelper {

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public ILaunchConfiguration createRunAsJavaApplicationLaunchConfiguration(
			IResource resource, 
			String launchConfigName,
			String launchConfigType,
			String mainClass,
			String projectName) {
		
		ILaunchConfigurationWorkingCopy newInstance = createLaunchConfiguration(
				resource, launchConfigName, launchConfigType, 
				mainClass,
				projectName);
		if (newInstance == null) {
			JLoopPlugin.logError("Can't create launch configuration", null);
			return null;
		}

		try {
			ILaunchConfiguration configuration = newInstance.doSave();
			return configuration;
		} catch (CoreException e) {
			JLoopPlugin.logError("Can't save launch configuration", e);
			return null;
		}
	}
	
	public ILaunchConfiguration createRunAsJUnitTestConfiguration(IResource resource, String launchConfigName, String launchConfigType) {
		String mainClass = "LoopTestSuite";
		String projectName = "JUnitLoop";

		ILaunchConfigurationWorkingCopy newInstance = createLaunchConfiguration(
				resource, launchConfigName, launchConfigType, mainClass,
				projectName);
		if (newInstance == null) {
			return null;
		}

		newInstance.setAttribute("org.eclipse.jdt.junit.CONTAINER", "");
		newInstance.setAttribute("org.eclipse.jdt.junit.KEEPRUNNING_ATTR", false);
		newInstance.setAttribute("org.eclipse.jdt.junit.TEST_KIND", "org.eclipse.jdt.junit.loader.junit4");

		try {
			ILaunchConfiguration configuration = newInstance.doSave();
			return configuration;
		} catch (CoreException e) {
			JLoopPlugin.logError("Can't save launch configuration", e);
			return null;
		}
	}

	private ILaunchConfigurationWorkingCopy createLaunchConfiguration(
			IResource resource, 
			String launchConfigName,
			String launchConfigType, 
			String mainClass, 
			String projectName) {
		ILaunchConfigurationType launchConfigurationType = getLaunchManager().getLaunchConfigurationType(launchConfigType);
		List<String> mappedResourcePaths = Collections.singletonList(resource.getFullPath().toString());
		List<String> mappedResourceTypes = Collections.singletonList("1");

		ILaunchConfigurationWorkingCopy newInstance;
		try {
			newInstance = launchConfigurationType.newInstance(null, launchConfigName);
		} catch (CoreException e) {
			JLoopPlugin.logError("Can't create launch configuration", e);
			return null;
		}
		newInstance.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_PATHS", mappedResourcePaths);
		newInstance.setAttribute("org.eclipse.debug.core.MAPPED_RESOURCE_TYPES", mappedResourceTypes);

		newInstance.setAttribute("org.eclipse.jdt.launching.MAIN_TYPE", mainClass);
		newInstance.setAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", projectName);
		newInstance.setAttribute("org.eclipse.jdt.launching.VM_ARGUMENTS", "");
		return newInstance;
	}

	public void printLaunchConfigs() throws CoreException {
		ILaunchConfiguration[] launchConfigurations = getLaunchManager().getLaunchConfigurations();
		for (ILaunchConfiguration launchConfiguration : launchConfigurations) {
			printLaunchConfig(launchConfiguration);
		}
	}

	private void printLaunchConfig(ILaunchConfiguration launchConfiguration)
			throws CoreException {
		System.out.println(launchConfiguration.getName() + " : " + launchConfiguration.getType().getIdentifier());
		Map<?,?> attributes = launchConfiguration.getAttributes();
		for (Object key : attributes.keySet()) {
			Object value = attributes.get(key);
			System.out.println(key + " -> " + value);
		}
	}
}
