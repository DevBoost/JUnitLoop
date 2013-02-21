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
package de.devboost.eclipse.junitloop;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import de.devboost.eclipse.jdtutilities.JDTUtility;
import de.devboost.eclipse.jloop.JLoopPlugin;
import de.devboost.eclipse.junitloop.launch.TestSuiteProjectUpdater;

public class UpdateTestSuiteJob extends Job {

	private IDependencyProvider dependencyProvider = JUnitLoopPlugin.getDefault().getDependencyProvider();

	public UpdateTestSuiteJob() {
		super("Updating JUnitLoop test suite");
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		setRule(root);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		updateTestSuite(monitor);
		return Status.OK_STATUS;
	}
	
	private void updateTestSuite(IProgressMonitor monitor) {
		JUnitLoopPlugin plugin = JUnitLoopPlugin.getDefault();
		ResoureChangeCollector changeCollector = plugin.getChangeCollector();
		Set<IResource> resources = changeCollector.retrieveChanges();
		
		SearchContext context = new SearchContext();
		try {
			monitor.beginTask("Searching related tests", resources.size());
			searchRelatedTests(monitor, resources, context);
			monitor.done();
		} catch (JavaModelException e) {
			JLoopPlugin.logError("Exception while searching for related test cases.", e);
			return;
		}
		
		// update loop test suite
		TestSuiteProjectUpdater testSuiteProjectUpdater = new TestSuiteProjectUpdater();
		boolean success = testSuiteProjectUpdater.createProjectIfNeeded();
		if (!success) {
			return;
		}
		TestRunScheduler scheduler = new TestRunScheduler();
		scheduler.addFailedTests(context.getTestClasses());
		
		testSuiteProjectUpdater.updateLoopTestSuite();
	}

	private void searchRelatedTests(
			IProgressMonitor monitor,
			Set<IResource> resources,
			SearchContext context) throws JavaModelException {
		
		JLoopPlugin.logInfo("Searching related tests for " + resources.size() + " resource(s).", null);
		for (IResource resource : resources) {
			IPath path = resource.getFullPath();
			String pathString = path.toString();
			context.addPathToVisit(pathString);

			// first, add element itself (if it is a test)
			IJavaElement javaElement = new JDTUtility().getJavaElement(pathString);
			addElementIfTest(context, javaElement);
			monitor.worked(1);
		}
		
		// second, add referencing elements
		searchRelatedTests(context);
		
		for (String dependencyPath : context.getVisitedPaths()) {
			IJavaElement javaElement = new JDTUtility().getJavaElement(dependencyPath);
			addElementIfTest(context, javaElement);
		}
	}

	private void searchRelatedTests(IDependencyCollector context) throws JavaModelException {
		Set<String> pathsToVisit = context.getPathsToVisit();
		while (!pathsToVisit.isEmpty()) {
			// search for new elements
			Iterator<String> iterator = pathsToVisit.iterator();
			String nextPath = iterator.next();
			context.addVisitedPath(nextPath);
			iterator.remove();
			
			Set<String> dependencies = dependencyProvider.findDependencies(nextPath);
			context.addPathsToVisit(dependencies);
		}
	}

	private void addElementIfTest(ITestCollector testCollector,
			IJavaElement javaElement) throws JavaModelException {
		if (javaElement == null) {
			return;
		}
		IJavaElement compilationUnit = javaElement.getAncestor(IJavaElement.COMPILATION_UNIT);
		if (compilationUnit == null) {
			return;
		}
		if (compilationUnit instanceof ICompilationUnit) {
			ICompilationUnit iCompilationUnit = (ICompilationUnit) compilationUnit;
			IType[] types = iCompilationUnit.getTypes();
			for (IType type : types) {
				if (new TestCaseChecker().isTestCase(type)) {
					IResource correspondingResource = type.getResource();
					if (correspondingResource != null) {
						IProject correspondingProject = correspondingResource.getProject();
						if (correspondingProject != null) {
							String projectName = correspondingProject.getName();
							String qualifiedClassName = type.getFullyQualifiedName();
							// found a test
							TestClass testClass = new TestClass(projectName, qualifiedClassName);
							testCollector.addTestClass(testClass);
						}
					}
				}
			}
		}
	}
}
