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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import de.devboost.eclipse.jloop.launch.JLoopLaunchProjectUpdater;

/**
 * The Runner class is used to start and stop Java classes that run in a loop.
 * To do so, it uses different life cycle handlers depending on whether the 
 * class shall run in the same or in a new VM.
 */
class LoopRunner {
	
	private JDTHelper jdtHelper = new JDTHelper();
	
	public void runLoopFiles() {
		IType type = getLoopType();
		if (type == null) {
			return;
		}
		Set<String> requiredProjects = getRequiredProjects(type);
		if (requiredProjects == null) {
			return;
		}
		
		IResource resource = type.getResource();
		IProject project = resource.getProject();
		if (new JDTHelper().hasProblems(project.getName())) {
			JLoopPlugin.logInfo("Can't run class in loop because required project contains error(s).", null);
			return;
		}
		
		IObjectLifecycleHandler handler = getLifecycleHandler(type);
		if (handler == null) {
			String message = "Can't determine life cycle handler for class " + type.getFullyQualifiedName();
			JLoopPlugin.logWarning(message, null);
			return;
		}

		stopRunningInstances();
		handler.start();
		// we must remember the life cycle handle to invoke its stop() method
		// later on
		JLoopPlugin.getDefault().addLifecycleHandler(handler);
	}

	private IType getLoopType() {
		JLoopPlugin plugin = JLoopPlugin.getDefault();
		if (plugin == null) {
			return null;
		}
		IFile loopFile = plugin.getLoopFile();
		if (loopFile == null) {
			return null;
		}
		IType type = getType(loopFile);
		return type;
	}

	public void updateLaunchProject(Set<String> currentProjects) {
		IType type = getLoopType();
		if (type == null) {
			return;
		}
		Set<String> requiredProjects = getRequiredProjects(type);
		if (currentProjects != null && Collections.disjoint(currentProjects, requiredProjects)) {
			return;
		}
		
		JLoopLaunchProjectUpdater launchProjectUpdater = new JLoopLaunchProjectUpdater(type, isRunInNewVM(type));
		if (requiredProjects == null) {
			return;
		}
		launchProjectUpdater.updateLaunchProject(requiredProjects);
	}
	
	private Set<String> getRequiredProjects(IType type) {
		Set<String> requiredProjects;
		try {
			IJavaProject javaProject = type.getJavaProject();
			List<String> projectNames = Arrays.asList(javaProject.getRequiredProjectNames());
			requiredProjects = new LinkedHashSet<String>(projectNames);
			requiredProjects.add(javaProject.getProject().getName());
		} catch (JavaModelException e) {
			JLoopPlugin.logError("Can't determine list of required projects.", e);
			return null;
		}
		return requiredProjects;
	}

	private IObjectLifecycleHandler getLifecycleHandler(final IType type) {
		if (isRunInSameVM(type)) {
			return new RunInSameVMHandler(type);
		} else {
			if (isRunInNewVM(type)) {
				return new RunInNewVMHandler(type);
			} else {
				return null;
			}
		}
	}

	private boolean isRunInSameVM(final IType loopFileType) {
		IType[] allClasses;
		try {
			ITypeHierarchy hierarchy = loopFileType.newSupertypeHierarchy(null);
			allClasses = hierarchy.getAllClasses();
		} catch (JavaModelException e) { 
			allClasses = new IType[] {loopFileType};
		}
		for (IType type : allClasses) {
			IMethod runInSameVMMethod = 
					type.getMethod(IMagicMethodNames.RUN_IN_SAME_VM_METHOD_NAME, new String[0]);
			IMethod runInSameVMMethodWithParameter = 
					type.getMethod(IMagicMethodNames.RUN_IN_SAME_VM_METHOD_NAME, 
							new String[] { "QString;"});
			IMethod runInSameVMMethodWithParameterBinary = 
					type.getMethod(IMagicMethodNames.RUN_IN_SAME_VM_METHOD_NAME, 
							new String[] { "Ljava.lang.String;"});
			if (runInSameVMMethod.exists() 
					|| runInSameVMMethodWithParameter.exists()
					|| runInSameVMMethodWithParameterBinary.exists()) {
				return true;
			}
		}
		return false;

	}

	private boolean isRunInNewVM(final IType loopFileType) {
		IType[] allClasses;
		try {
			ITypeHierarchy hierarchy = loopFileType.newSupertypeHierarchy(null);
			allClasses = hierarchy.getAllClasses();
		} catch (JavaModelException e) { 
			allClasses = new IType[] {loopFileType};
		}
		for (IType type : allClasses) {
			IMethod runMethod = type.getMethod(IMagicMethodNames.RUN_IN_NEW_VM_METHOD_NAME, new String[0]);
			if (runMethod.exists()) {
				return true;
			}
		}
		return false;
	}

	private IType getType(IFile loopFile) {
		IJavaProject javaProject = jdtHelper.getJavaProject(loopFile.getProject());
		if (javaProject == null) {
			return null;
		}
		IPath path = loopFile.getFullPath();
		String pathString = path.toString();
		try {
			IType[] types = jdtHelper.findJavaTypes(pathString);
			if (types != null && types.length > 0) {
				return types[0];
			}
		} catch (JavaModelException jme) {
			JLoopPlugin.logError("Can't determine Java type for " + pathString, jme);
		}
		return null;
	}

	private void stopRunningInstances() {
		Set<IObjectLifecycleHandler> handlers = JLoopPlugin.getDefault().getLifecycleHandlers();
		Iterator<IObjectLifecycleHandler> iterator = handlers.iterator();
		while (iterator.hasNext()) {
			IObjectLifecycleHandler handler = iterator.next();
			iterator.remove();
			handler.stop();
		}
	}
}
