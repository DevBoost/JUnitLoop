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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import de.devboost.eclipse.jdtutilities.JDTUtility;

public class JDTHelper {

	/**
	 * Checks whether the given project (or one of its transitive dependencies)
	 * contains errors.
	 * 
	 * @param projectName the name of the project to check
	 * @return true if there is errors, false if not
	 */
	public boolean hasProblems(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		Set<String> requiredProjects = new LinkedHashSet<String>();
		
		Set<String> projectsToAnalyze = new LinkedHashSet<String>();
		projectsToAnalyze.add(projectName);
		
		while (!projectsToAnalyze.isEmpty()) {
			Iterator<String> iterator = projectsToAnalyze.iterator();
			IProject project = root.getProject(iterator.next());
			iterator.remove();
			if (project != null) {
				requiredProjects.add(project.getName());
				String[] dependencies = getRequiredProjects(project);
				for (String dependency : dependencies) {
					if (!requiredProjects.contains(dependency)) {
						projectsToAnalyze.add(dependency);
					}
					requiredProjects.add(dependency);
				}
			}
		}
		
		for (String requiredProject : requiredProjects) {
			if (hasProblems(root.getProject(requiredProject))) {
				return true;
			}
		}
		return false;
	}

	// This code is copied from org.eclipse.debug.core.model.LaunchConfigurationDelegate.
	private boolean hasProblems(IProject project) {
		try {
			IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			int markerCount = markers.length;
			if (markerCount > 0) {
				for (int i = 0; i < markerCount; i++) {
					if (isLaunchProblem(markers[i])) {
						return true;
					}
				}
			}
		} catch (CoreException ce) {
			JLoopPlugin.logError("Exception while checking project for error markers.", ce);
		}
		
		return false;
	}

	private String[] getRequiredProjects(IProject project) {
		IJavaProject javaProject = new JDTUtility().getJavaProject(project);
		if (javaProject != null) {
			try {
				String[] requiredProjectNames = javaProject.getRequiredProjectNames();
				return requiredProjectNames;
			} catch (JavaModelException e) {
				JLoopPlugin.logError("Exception while determining project dependencies.", e);
			}
		}
		return new String[0];
	}
	
	// This code is copied from org.eclipse.debug.core.model.LaunchConfigurationDelegate.
	private boolean isLaunchProblem(IMarker problemMarker) throws CoreException {
		Integer severity = (Integer)problemMarker.getAttribute(IMarker.SEVERITY);
		if (severity != null) {
			return severity.intValue() >= IMarker.SEVERITY_ERROR;
		} 
		
		return false;
	}

	public boolean hasStopMethod(IType type) {
		IType[] allClasses = new JDTHelper().getTypeAndAllSuperTypes(type);
		for (IType superType : allClasses) {
			IMethod stopMethod = superType.getMethod(IMagicMethodNames.STOP_METHOD_NAME, new String[0]);
			if (stopMethod.exists()) {
				// TODO check visibility (must be public)
				return true;
			}
		}
		return false;
	}

	public IType[] getTypeAndAllSuperTypes(IType type) {
		IType[] allClasses;
		try {
			ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
			allClasses = hierarchy.getAllClasses();
		} catch (JavaModelException e) { 
			allClasses = new IType[] {type};
		}
		return allClasses;
	}
}
