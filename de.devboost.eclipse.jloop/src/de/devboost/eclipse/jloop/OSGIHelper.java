/*******************************************************************************
 * Copyright (c) 2006-2013
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The OSGIHelper can be used to install and uninstall OSGi bundles.
 */
class OSGIHelper {
	
	/**
	 * Installs the bundle with the given name. If the bundle is already
	 * installed, it is uninstalled beforehand.
	 * 
	 * @param projectName the name of the bundle to install
	 * @return the installed bundle
	 */
	public Bundle installBundle(String projectName) {
		 //TODO only works with "platform:" URIs
		String projectUri = getProjectFile(projectName).toURI().toString();
		//-----------
		Bundle bundle = null;
		try {
			// bundle fails to load if project name contains a space
			projectUri = projectUri.replaceAll("%20", " "); //$NON-NLS-1$ //$NON-NLS-2$
			Bundle installedBundle = Platform.getBundle(projectName);
			if (installedBundle != null) {
				installedBundle.uninstall();
			}
			
			JLoopPlugin jLoopPlugin = JLoopPlugin.getDefault();
			Bundle jLoopBundle = jLoopPlugin.getBundle();
			BundleContext jLoopBundleContext = jLoopBundle.getBundleContext();
			bundle = jLoopBundleContext.installBundle(projectUri);
			if (bundle == null) {
				throw new RuntimeException();
			}
		} catch (Exception e) {
			final String message = "Failed to load bundle: " + projectName; //$NON-NLS-1$
			JLoopPlugin.logError(message, e);
			return bundle;
		}
		JLoopPlugin.logInfo("Bundle loaded: " + projectName, null); //$NON-NLS-1$
		return bundle;
	}

	private File getProjectFile(String projectName) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectName);
		return project.getLocation().toFile();
	}
}
