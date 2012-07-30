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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class JDTHelper {

	private boolean isJavaProject(IProject project) {
		if (project == null) {
			return false;
		}
		try {
			return project.isNatureEnabled("org.eclipse.jdt.core.javanature");
		} catch (CoreException e) {
		}
		return false;
	}

	private IJavaProject getJavaProject(IProject project) {
		return (isJavaProject(project) ? JavaCore.create(project) : null);
	}

	public IJavaProject getJavaProject(IResource resource) {
		return getJavaProject(resource.getProject());
	}

	public IJavaElement findJavaElement(String path) throws JavaModelException {
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile file = root.getFile(new Path(path));
		IJavaProject javaProject = new JDTHelper().getJavaProject(file);
		if (javaProject == null) {
			JLoopPlugin.logError("Java project for file not found: " + path, null);
			return null;
		}
		IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
		for (IPackageFragmentRoot packageFragmentRoot : roots) {
			IPath fragmentPath = packageFragmentRoot.getPath();
			String fragmentPathString = fragmentPath.toString();
			if (path.startsWith(fragmentPathString + "/")) {
				// resource is contained in this package fragment root
				String classPathRelativePath = path.substring(fragmentPathString.length() + 1);
				IJavaElement element = javaProject.findElement(new Path(classPathRelativePath));
				if (element != null) {
					return element;
				}
			}
		}
 		return null;
	}

	public IType[] findJavaTypes(String path) throws JavaModelException {
		IJavaElement javaElement = findJavaElement(path);
		if (javaElement instanceof ICompilationUnit) {
			ICompilationUnit compilationUnit = (ICompilationUnit) javaElement;
			IType[] types = compilationUnit.getTypes();
			return types;
		}
		return null;
	}

	/**
	 * Checks whether the given project (or one of its transitive dependencies)
	 * contains errors.
	 * 
	 * @param projectName the name of the project to check
	 * @return true if there is errors, false if not
	 */
	public boolean existsProblems(String projectName) {
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
			if (existsProblems(root.getProject(requiredProject))) {
				return true;
			}
		}
		return false;
	}

	// This code is copied from org.eclipse.debug.core.model.LaunchConfigurationDelegate.
	private boolean existsProblems(IProject project) {
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
		IJavaProject javaProject = new JDTHelper().getJavaProject(project);
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
}
