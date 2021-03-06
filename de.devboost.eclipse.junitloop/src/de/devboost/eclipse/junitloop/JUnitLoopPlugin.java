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

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.devboost.eclipse.jloop.launch.LaunchingHelper;
import de.devboost.eclipse.junitloop.launch.TestSuiteProjectData;
import de.devboost.eclipse.junitloop.launch.TestSuiteProjectUpdater;

/**
 * The activator class controls the plug-in life cycle
 */
public class JUnitLoopPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.devboost.eclipse.junitloop"; //$NON-NLS-1$

	public static final String JDT_JUNIT_LAUNCH_CONFIG = "org.eclipse.jdt.junit.launchconfig";

	// The shared instance
	private static JUnitLoopPlugin plugin;

	private boolean enabled;

	private IDependencyProvider dependencyProvider;

	private boolean testSessionIsRunning;

	private boolean runTestsAfterSessionFinished;
	
	private ProcessTerminationListener processTerminationListener = new ProcessTerminationListener();

	private ChangeSkipManager changeSkipManager = new ChangeSkipManager();

	private ResoureChangeCollector changeCollector = new ResoureChangeCollector();

	/**
	 * The constructor
	 */
	public JUnitLoopPlugin() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		DebugPlugin.getDefault().addDebugEventListener(processTerminationListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		DebugPlugin.getDefault().removeDebugEventListener(processTerminationListener);
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JUnitLoopPlugin getDefault() {
		return plugin;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		// previously, the test suite project was created when the first class
		// was changed. Now, we create it when JUnitLoop is activated to make
		// sure the project exists upon the first class change. This avoids
		// the problem that the test suite was not executed if it did not exist
		// yet.
		if (enabled) {
			TestSuiteProjectUpdater testSuiteProjectUpdater = new TestSuiteProjectUpdater();
			testSuiteProjectUpdater.createProjectIfNeeded();
			testSuiteProjectUpdater.updateLoopTestSuite();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public synchronized IDependencyProvider getDependencyProvider() {
		if (dependencyProvider == null) {
			IDependencyProvider jdtDependencyProvider = new JDTDependencyProvider();
			dependencyProvider = new MemoryDependencyCache(jdtDependencyProvider);
		}
		return dependencyProvider;
	}

	public void notifySessionLaunched() {
		testSessionIsRunning = true;
	}

	public void notifySessionFinished() {
		testSessionIsRunning = false;
		if (runTestsAfterSessionFinished) {
			runTestsAfterSessionFinished = false;
			launchTestSuite();
		}
	}

	public void launchTestSuite() {
		if (testSessionIsRunning) {
			runTestsAfterSessionFinished = true;
			return;
		} else {
			String sourcePath = new TestSuiteProjectData().getSourcePath();
			String launchType = JUnitLoopPlugin.JDT_JUNIT_LAUNCH_CONFIG;
			String jobName = "JUnit Loop Launch Job";
			new LaunchingHelper().launchTestConfiguration(sourcePath, launchType, jobName);
		}
	}

	public ChangeSkipManager getChangeSkipManager() {
		return changeSkipManager;
	}

	public ResoureChangeCollector getChangeCollector() {
		return changeCollector;
	}
}
