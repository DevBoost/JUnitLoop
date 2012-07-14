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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

import de.devboost.eclipse.jloop.IPreLaunchHook;
import de.devboost.eclipse.jloop.JLoopPlugin;

class LauchJob extends Job {
	
	private ILaunchConfiguration launchConfiguration;
	private IPreLaunchHook preLaunchHook;

	public LauchJob(ILaunchConfiguration launchConfiguration, String name, IPreLaunchHook preLaunchHook) {
		super(name);
		this.launchConfiguration = launchConfiguration;
		this.preLaunchHook = preLaunchHook;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		try {
			ILaunchConfigurationWorkingCopy workingCopy = launchConfiguration.getWorkingCopy();
			runBeforeLaunch(workingCopy);
			ILaunchConfiguration modifiedConfig = workingCopy.doSave();
			modifiedConfig.launch(ILaunchManager.RUN_MODE, monitor);
		} catch (CoreException e) {
			JLoopPlugin.logError("Can't launch launch configuration.", e);
		}
		return Status.OK_STATUS;
	}

	private void runBeforeLaunch(ILaunchConfigurationWorkingCopy workingCopy) {
		if (preLaunchHook != null) {
			preLaunchHook.runBeforeLaunch(workingCopy);
		}
	}
}