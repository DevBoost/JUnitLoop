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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * The AbstractLaunchProjectUpdater is used to create or update a special 
 * project that is used to run the classes or tests that have were changed or
 * affected by a change.
 */
public abstract class AbstractLaunchProjectUpdater {

	private IWorkspaceRoot workspaceRoot;
	private AbstractLaunchProjectData projectData;
	
	public AbstractLaunchProjectUpdater(AbstractLaunchProjectData projectData) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspaceRoot = workspace.getRoot();
		this.projectData = projectData;
	}
	
	protected AbstractLaunchProjectData getProjectData() {
		return projectData;
	}

	protected void updateLaunchProject(Set<String> requiredProjects) {
		if (!updateRequired()) {
			return;
		}
		// create loop launch project in workspace if it does not exist
		boolean success = createProjectIfNeeded();
		if (!success) {
			return;
		}
		
		IJavaProject javaProject = getLaunchProject();
		try {
			updateProjectClasspath(javaProject, requiredProjects);
		} catch (JavaModelException e) {
			JLoopPlugin.logError("Can't update classpath of " + getLongProjectName() + " project.", e);
		} catch (CoreException e) {
			JLoopPlugin.logError("Can't update classpath of " + getLongProjectName() + " project.", e);
		}

		IPath path = new Path(projectData.getSourcePath());
		IFile file = workspaceRoot.getFile(path);
		String code = getSourceCode();

		InputStream source = new ByteArrayInputStream(code.getBytes());
		try {
			if (!file.exists()) {
				createParents(file.getParent());
				file.create(source, true, new NullProgressMonitor());
			} else {
				file.setContents(source, true, false, new NullProgressMonitor());
			}
		} catch (CoreException ce) {
			JLoopPlugin.logError("Can't update " + getLongProjectName() + " class.", ce);
		}
	}

	/**
	 * Subclasses can implement this method to indicate whether an update of the
	 * launch project is required. If this method returns false, no actions will
	 * be performed.
	 * 
	 * @return
	 */
	protected abstract boolean updateRequired();

	private void createParents(IContainer container) {
		try {
			IFolder folder = workspaceRoot.getFolder(container.getFullPath());
			folder.create(true, true, new NullProgressMonitor());
		} catch (CoreException e) {
			JLoopPlugin.logError("Can't create folder in " + getLongProjectName() + " project.", e);
		}
	}

	public boolean createProjectIfNeeded() {
		try {
			// create loop test suite project in workspace if it does not exist
			IProject loopSuiteProject = getProject();
			if (!loopSuiteProject.exists()) {
				loopSuiteProject.create(new NullProgressMonitor());
			}
			loopSuiteProject.open(new NullProgressMonitor());
			updateProjectNature(loopSuiteProject);
			// get java project to create it if needed
			getLaunchProject();
			return true;
		} catch (CoreException ce) {
			JLoopPlugin.logError("Can't create " + getLongProjectName() + " project.", ce);
			return false;
		}
	}

	private IJavaProject getLaunchProject() {
		return JavaCore.create(getProject());
	}

	public IProject getProject() {
		return workspaceRoot.getProject(projectData.getProjectName());
	}

	protected void updateProjectClasspath(IJavaProject javaProject, Set<String> requiredProjects)
			throws CoreException, JavaModelException {
		
		IProject loopSuiteProject = javaProject.getProject();
		IClasspathEntry[] entries = getClasspathEntries(loopSuiteProject, requiredProjects);
		IPath outputLocation = loopSuiteProject.getFullPath().append("/bin");
		IFolder outputFolder = workspaceRoot.getFolder(outputLocation);
		if (!outputFolder.exists()) {
			outputFolder.create(true, true, new NullProgressMonitor());
		}
		IProgressMonitor monitor = new NullProgressMonitor();
		javaProject.setRawClasspath(entries, outputLocation, monitor);
		loopSuiteProject.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		
		loopSuiteProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
	}

	private void updateProjectNature(IProject project) throws CoreException {
		if (!project.hasNature(JavaCore.NATURE_ID)) {
			try {
				IProjectDescription descriptions = project.getDescription();
				String[] oldIds = descriptions.getNatureIds();
				String[] newIds = new String[oldIds.length + 1];
				System.arraycopy(oldIds, 0, newIds, 0, oldIds.length);
				newIds[oldIds.length] = JavaCore.NATURE_ID;
				descriptions.setNatureIds(newIds);
				project.setDescription(descriptions, null);
			} catch (CoreException e) {
				JLoopPlugin.logWarning("Could not add Java nature to project " + project, e);
			}
		}
	}

	private IClasspathEntry[] getClasspathEntries(IProject project, Set<String> testProjects) {
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();

		IPath path = project.getFullPath().append(projectData.getSourceFolder());
		IClasspathEntry srcEntry = JavaCore.newSourceEntry(path);
		path = new Path("org.eclipse.jdt.launching.JRE_CONTAINER");
		IClasspathEntry jdkEntry = JavaCore.newContainerEntry(path);
		
		entries.add(srcEntry);
		entries.add(jdkEntry);
		entries.addAll(getAdditionalClassPathEntries());

		Iterator<String> iterator = testProjects.iterator();
		while (iterator.hasNext()) {
			String projectName = iterator.next();
			
			IProject nextProject = project.getWorkspace().getRoot().getProject(projectName);
			path = nextProject.getFullPath();
			IClasspathEntry projectEntry = JavaCore.newProjectEntry(path);
			entries.add(projectEntry);
		}
		return entries.toArray(new IClasspathEntry[entries.size()]);
	}

	protected abstract Collection<IClasspathEntry> getAdditionalClassPathEntries();

	protected abstract String getLongProjectName();

	protected abstract String getSourceCode();

}
