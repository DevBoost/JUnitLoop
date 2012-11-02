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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.RuntimeProcess;

import de.devboost.eclipse.jloop.JLoopPlugin;
import de.devboost.eclipse.jloop.launch.LauchConfigurationHelper;
import de.devboost.eclipse.junitloop.launch.TestSuiteProjectUpdater;

/**
 * The {@link ProcessTerminationListener} is used to listen to events thrown by
 * the Eclipse Debug framework. The listener is specifically interested in 
 * terminations of JUnitLoop process. This required to signal manual 
 * terminations of such processes to the {@link JUnitLoopPlugin}. Such 
 * terminations are not recognized by the {@link JUnitLoopTestRunListener}.
 */
class ProcessTerminationListener implements IDebugEventSetListener {

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent event : events) {
			int kind = event.getKind();
			if (kind != DebugEvent.TERMINATE) {
				continue;
			}

			Object source = event.getSource();
			if (source == null) {
				continue;
			}
			
			if (source instanceof RuntimeProcess) {
				RuntimeProcess process = (RuntimeProcess) source;
				ILaunch launch = process.getLaunch();
				if (launch == null) {
					continue;
				}
				
				ILaunchConfiguration configuration = launch.getLaunchConfiguration();
				if (configuration == null) {
					continue;
				}
				
				try {
					String mainTypeKey = LauchConfigurationHelper.JDT_LAUNCH_CONFIG_KEY_MAIN_TYPE;
					String mainType = configuration.getAttribute(mainTypeKey, (String) null);
					if (TestSuiteProjectUpdater.LOOP_TEST_SUITE_NAME.equals(mainType)) {
						// a running JUnitLoop test suite was terminated
						JUnitLoopPlugin.getDefault().notifySessionFinished();
					}
				} catch (CoreException e) {
					JLoopPlugin.logError("Exception while analyzing launch configuration.", e);
				}
			}
		}
	}
}