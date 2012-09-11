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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class JLoopPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.devboost.eclipse.jloop"; //$NON-NLS-1$

	// The shared instance
	private static JLoopPlugin plugin;

	private IFile loopFile = null;

	private Set<IObjectLifecycleHandler> lifecycleHandlers = new LinkedHashSet<IObjectLifecycleHandler>();

	/**
	 * The constructor
	 */
	public JLoopPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static JLoopPlugin getDefault() {
		return plugin;
	}

	public void setLoopFile(IFile file) {
		loopFile = file;
		new LoopRunner().updateLaunchProject(null);
	}
	
	public IFile getLoopFile() {
		return loopFile;
	}

	/**
	 * Helper method for error logging.
	 * 
	 * @param message the error message to log
	 * @param throwable the exception that describes the error in detail (can be null)
	 * 
	 * @return the status object describing the error
	 */
	public static org.eclipse.core.runtime.IStatus logError(String message, Throwable throwable) {
		return log(org.eclipse.core.runtime.IStatus.ERROR, message, throwable);
	}
	
	/**
	 * Helper method for logging informations.
	 * 
	 * @param message the information message to log
	 * @param throwable the exception that describes the information in detail (can be
	 * null)
	 * 
	 * @return the status object describing the warning
	 */
	public static org.eclipse.core.runtime.IStatus logInfo(String message, Throwable throwable) {
		return log(org.eclipse.core.runtime.IStatus.INFO, message, throwable);
	}
	
	/**
	 * Helper method for logging warnings.
	 * 
	 * @param message the warning message to log
	 * @param throwable the exception that describes the warning in detail (can be
	 * null)
	 * 
	 * @return the status object describing the warning
	 */
	public static org.eclipse.core.runtime.IStatus logWarning(String message, Throwable throwable) {
		return log(org.eclipse.core.runtime.IStatus.WARNING, message, throwable);
	}
	
	/**
	 * Helper method for logging.
	 * 
	 * @param type the type of the message to log
	 * @param message the message to log
	 * @param throwable the exception that describes the error in detail (can be null)
	 * 
	 * @return the status object describing the error
	 */
	protected static org.eclipse.core.runtime.IStatus log(int type, String message, Throwable throwable) {
		org.eclipse.core.runtime.IStatus status;
		if (throwable != null) {
			status = new org.eclipse.core.runtime.Status(type, JLoopPlugin.PLUGIN_ID, 0, message, throwable);
		} else {
			status = new org.eclipse.core.runtime.Status(type, JLoopPlugin.PLUGIN_ID, message);
		}
		final JLoopPlugin pluginInstance = JLoopPlugin.getDefault();
		if (pluginInstance == null) {
			System.err.println(message);
			if (throwable != null) {
				throwable.printStackTrace();
			}
		} else {
			pluginInstance.getLog().log(status);
		}
		return status;
	}

	public void addLifecycleHandler(IObjectLifecycleHandler handler) {
		lifecycleHandlers.add(handler);
	}

	public Set<IObjectLifecycleHandler> getLifecycleHandlers() {
		return lifecycleHandlers;
	}
}
