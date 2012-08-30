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
package de.devboost.eclipse.jloop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IType;

import de.devboost.eclipse.jloop.launch.JLoopLaunchProjectData;
import de.devboost.eclipse.jloop.launch.JLoopLaunchProjectUpdater;
import de.devboost.eclipse.jloop.launch.LauchConfigurationHelper;
import de.devboost.eclipse.jloop.launch.LaunchingHelper;

class RunInNewVMHandler implements IObjectLifecycleHandler {
	
	/**
	 * A temporary file that is created before the new VM is launched. The
	 * JLoop wrapper class will periodically read from this file to check 
	 * whether it must stop. 
	 */
	private File tempFile;

	private class StopFileCreator implements IPreLaunchHook {
	
		@Override
		public void runBeforeLaunch(ILaunchConfigurationWorkingCopy workingCopy) {
			try {
				tempFile = File.createTempFile(RunInNewVMHandler.class.getName(), "");
				workingCopy.setAttribute("org.eclipse.jdt.launching.PROGRAM_ARGUMENTS", tempFile.getAbsolutePath());
				tempFile.deleteOnExit();
			} catch (IOException e) {
				JLoopPlugin.logError("Can't create stop signal file.", e);
			}
		}
	}

	private final IType type;
	private boolean hasStopMethod;

	public RunInNewVMHandler(IType type) {
		super();
		this.type = type;
		this.hasStopMethod = type.getMethod(IMagicMethodNames.STOP_METHOD_NAME, new String[0]).exists();
	}

	@Override
	public void start() {
		startNewInstanceInNewVM(type);
	}

	@Override
	public void stop() {
		if (!hasStopMethod) {
			return;
		}
		try {
			FileWriter writer = new FileWriter(tempFile);
			writer.write(IMagicMethodNames.STOP_METHOD_NAME + "\n");
			writer.close();
		} catch (IOException e) {
			JLoopPlugin.logError("Can't write to stop signal file.", e);
		}
	}

	private void startNewInstanceInNewVM(IType type) {
		JLoopLaunchProjectData launchProjectData = new JLoopLaunchProjectData();
		
		JLoopLaunchProjectUpdater launchProjectUpdater = new JLoopLaunchProjectUpdater(type, true);
		
		Path sourcePath = new Path(launchProjectData.getSourcePath());
		IProject project = launchProjectUpdater.getProject();
		IResource resource = project.getFile(sourcePath);
		String launchConfigName = "JLoop";
		String launchConfigType = "org.eclipse.jdt.launching.localJavaApplication";
		String mainClass = launchProjectData.getMainClassName();
		String projectName = launchProjectData.getProjectName();
		LauchConfigurationHelper helper = new LauchConfigurationHelper();
		ILaunchConfiguration launchConfiguration = helper.createRunAsJavaApplicationLaunchConfiguration(resource, launchConfigName, launchConfigType, mainClass, projectName);

		String jobName = "JLoop Launch Job";
		IPreLaunchHook preLaunchHook = null;
		if (hasStopMethod) {
			preLaunchHook = new StopFileCreator();
		}
		new LaunchingHelper().launchWithJob(launchConfiguration, jobName, preLaunchHook);
	}
}