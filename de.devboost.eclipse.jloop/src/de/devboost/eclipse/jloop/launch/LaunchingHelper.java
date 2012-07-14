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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;

import de.devboost.eclipse.jloop.IPreLaunchHook;

public class LaunchingHelper {

	public void launchTestConfiguration(String sourcePath, String launchConfigType, String launchJobName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		Path path = new Path(sourcePath);
		IFile mainClass = root.getFile(path);
		if (!mainClass.exists()) {
			return;
		}
		launchTestConfiguration(mainClass, launchConfigType, launchJobName);
	}
	
	@SuppressWarnings("restriction")
	private void launchTestConfiguration(IResource resource, String launchConfigType, String launchJobName) {
		String[] types = new String[] {launchConfigType};
		org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager launchConfigurationManager = 
				org.eclipse.debug.internal.ui.DebugUIPlugin.getDefault().getLaunchConfigurationManager();
		ILaunchConfiguration[] applicableLaunchConfigurations = launchConfigurationManager.getApplicableLaunchConfigurations(types, resource);
		if (applicableLaunchConfigurations != null && applicableLaunchConfigurations.length > 0) {
			ILaunchConfiguration launchConfiguration = applicableLaunchConfigurations[0];
			launchWithJob(launchConfiguration, launchJobName, null);
		}
	}

	public void launchWithJob(ILaunchConfiguration launchConfiguration, String launchJobName, IPreLaunchHook preLaunchHook) {
		Job launchJob = new LauchJob(launchConfiguration, launchJobName, preLaunchHook);
		launchJob.schedule();
	}
}
