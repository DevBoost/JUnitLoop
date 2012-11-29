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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

import de.devboost.eclipse.jloop.JDTHelper;
import de.devboost.eclipse.jloop.JLoopPlugin;

class JDTDependencyProvider implements IDependencyProvider {

	private class DependencySearchRequestor extends SearchRequestor {
		
		private Set<String> result;

		private DependencySearchRequestor(Set<String> result) {
			this.result = result;
		}

		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			Object foundElement = match.getElement();
			if (foundElement instanceof IJavaElement) {
				IJavaElement javaElement = (IJavaElement) foundElement;
				addClassToSearchResult(result, javaElement);
			}
		}
	}
	
	@Override
	public Set<String> findDependencies(String path) {
		try {
			return searchRelatedClasses(path);
		} catch (JavaModelException e) {
			JLoopPlugin.logError("Exception while searching for dependencies.", e);
			return Collections.emptySet();
		}
	}

	private Set<String> searchRelatedClasses(String path) throws JavaModelException {
		return searchReferences(path);
	}

	private Set<String> searchReferences(String path) throws JavaModelException {
		// mark this element as visited
		IType[] types = new JDTHelper().findJavaTypes(path);
		if (types == null) {
			return Collections.emptySet();
		}
		if (types.length == 0) {
			return Collections.emptySet();
		}
		
		SearchEngine engine = new SearchEngine();
		SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
		IJavaSearchScope scope = createScope();
		Set<String> result = new LinkedHashSet<String>();
		SearchRequestor requestor = new DependencySearchRequestor(result);
		IProgressMonitor monitor = new NullProgressMonitor();
		for (IType type : types) {
			SearchPattern pattern = SearchPattern.createPattern(type, IJavaSearchConstants.REFERENCES);
			try {
				engine.search(pattern, participants, scope, requestor, monitor);
			} catch (CoreException e) {
				JLoopPlugin.logError("Exception while searching for class dependencies.", e);
			}
		}
		return result;
	}

	@SuppressWarnings("restriction")
	private IJavaSearchScope createScope() {
		return org.eclipse.jdt.internal.core.JavaModelManager.getJavaModelManager().getWorkspaceScope();
	}

	private void addClassToSearchResult(Set<String> result, IJavaElement javaElement)
			throws JavaModelException {
		if (javaElement == null) {
			return;
		}
		IResource resource = javaElement.getResource();
		if (resource == null) {
			return;
		}
		String path = resource.getFullPath().toString();
		result.add(path);
	}

	@Override
	public void clear(String path) {
		// do nothing
	}
}
