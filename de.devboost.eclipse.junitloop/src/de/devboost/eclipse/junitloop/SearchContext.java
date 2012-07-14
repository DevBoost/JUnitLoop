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
package de.devboost.eclipse.junitloop;

import java.util.LinkedHashSet;
import java.util.Set;

class SearchContext implements IDependencyCollector, ITestCollector {
	
	private Set<String> pathsToVisit = new LinkedHashSet<String>();
	private Set<String> testProjects = new LinkedHashSet<String>();
	private Set<String> testClasses = new LinkedHashSet<String>();
	private Set<String> visitedPaths = new LinkedHashSet<String>();

	/* (non-Javadoc)
	 * @see de.devboost.eclipse.junitloop.IDependencyCollector#getVisitedPaths()
	 */
	@Override
	public Set<String> getVisitedPaths() {
		return visitedPaths;
	}

	/* (non-Javadoc)
	 * @see de.devboost.eclipse.junitloop.IDependencyCollector#addPathToVisit(java.lang.String)
	 */
	@Override
	public void addPathToVisit(String path) {
		pathsToVisit.add(path);
	}

	/* (non-Javadoc)
	 * @see de.devboost.eclipse.junitloop.ITestCollector#addTestClass(java.lang.String)
	 */
	@Override
	public void addTestClass(String testClass) {
		testClasses.add(testClass);
	}

	/* (non-Javadoc)
	 * @see de.devboost.eclipse.junitloop.ITestCollector#addTestProject(java.lang.String)
	 */
	@Override
	public void addTestProject(String testProject) {
		testProjects.add(testProject);
	}

	/* (non-Javadoc)
	 * @see de.devboost.eclipse.junitloop.IDependencyCollector#addVisitedPath(java.lang.String)
	 */
	@Override
	public void addVisitedPath(String path) {
		visitedPaths.add(path);
	}

	/* (non-Javadoc)
	 * @see de.devboost.eclipse.junitloop.IDependencyCollector#getPathsToVisit()
	 */
	@Override
	public Set<String> getPathsToVisit() {
		return pathsToVisit;
	}

	/* (non-Javadoc)
	 * @see de.devboost.eclipse.junitloop.ITestCollector#getTestClasses()
	 */
	@Override
	public Set<String> getTestClasses() {
		return testClasses;
	}

	/* (non-Javadoc)
	 * @see de.devboost.eclipse.junitloop.ITestCollector#getTestProjects()
	 */
	@Override
	public Set<String> getTestProjects() {
		return testProjects;
	}

	@Override
	public void addPathsToVisit(Set<String> dependencies) {
		if (dependencies == null) {
			return;
		}
		for (String dependency : dependencies) {
			if (visitedPaths.contains(dependency)) {
				continue;
			}
			pathsToVisit.add(dependency);
		}
	}
}
