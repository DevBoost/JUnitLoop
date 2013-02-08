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
package de.devboost.eclipse.junitloop;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import de.devboost.eclipse.jloop.launch.LauchConfigurationHelper;
import de.devboost.eclipse.junitloop.launch.TestSuiteProjectData;

/**
 * The ChangeHandler class is responsible to take appropriate actions when 
 * classes are compiled in Eclipse. It clears the dependency cache for each
 * compiled class. Also, if JUnitLoop is enabled, the loop test suite is
 * updated and launched.
 */
class ChangeHandler {

	private IDependencyProvider dependencyProvider;
	
	public ChangeHandler() {
		super();
		JUnitLoopPlugin plugin = JUnitLoopPlugin.getDefault();
		if (plugin != null) {
			plugin.getDependencyProvider();
		}
	}

	public void handleChange(List<IResource> resources) {
		JUnitLoopPlugin plugin = JUnitLoopPlugin.getDefault();
		if (plugin == null) {
			return;
		}
		boolean isJUnitLoopEnabled = plugin.isEnabled();

		Set<IResource> nonJLoopResources = new LinkedHashSet<IResource>();
		Set<IResource> jLoopResources = new LinkedHashSet<IResource>();
		
		String sourcePath = new TestSuiteProjectData().getSourcePath();
		for (IResource resource : resources) {
			IPath fullPath = resource.getFullPath();
			if (sourcePath.equals(fullPath.toString())) {
				jLoopResources.add(resource);
			} else {
				nonJLoopResources.add(resource);
			}
		}
		
		if (!nonJLoopResources.isEmpty()) {
			clearCache(nonJLoopResources);
			// TODO only consider resources that be not compile in batch mode?
			// if we do so, clean build do not trigger a JUnit run. do we want
			// this? maybe a preference option is appropriate here?
			if (/**!isBatch && **/isJUnitLoopEnabled) {
				// execute dependency calculation in separate job(s) to make 
				// sure that the build job is not blocked for a long time
				plugin.getChangeCollector().addChanges(nonJLoopResources);
			}
		}
		if (!jLoopResources.isEmpty() && isJUnitLoopEnabled) {
			// do this only when the loop test suite has changed
			ChangeSkipManager changeSkipManager = plugin.getChangeSkipManager();
			if (changeSkipManager.shallSkip()) {
				createAndRunJUnitLoopLaunchConfiguration(jLoopResources.iterator().next());
			}
		}
	}

	private void clearCache(Set<IResource> nonJLoopResources) {
		for (IResource resource : nonJLoopResources) {
			String path = resource.getFullPath().toString();
			if (dependencyProvider != null) {
				dependencyProvider.clear(path);
			}
		}
	}

	private void createAndRunJUnitLoopLaunchConfiguration(IResource resource) {
		// we construct a custom JUnit launch configuration that runs all 
		// tests which are related to the changed classes.
		LauchConfigurationHelper helper = new LauchConfigurationHelper();
		helper.createRunAsJUnitTestConfiguration(resource, "JUnitLoop", JUnitLoopPlugin.JDT_JUNIT_LAUNCH_CONFIG);

		JUnitLoopPlugin.getDefault().launchTestSuite();
	}
}
