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

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import de.devboost.eclipse.junitloop.launch.TestSuiteProjectData;

class TestCaseChecker {

	public boolean isTestCase(IType type) throws JavaModelException {
		if (Flags.isAbstract(type.getFlags())) {
			return false;
		}
		if (type.isMember()) {
			return false;
		}
		if (!type.isClass()) {
			return false;
		}
		if (new TestSuiteProjectData().getMainClassName().equals(type.getFullyQualifiedName())) {
			return false;
		}
		// TODO this check is not sufficient. there might be test classes that 
		// use JUnit annotations instead of inheriting from TestCase.
		
		// TODO do we need to cache the type hierarchy?
		ITypeHierarchy supertypeHierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
		IType[] superTypes = supertypeHierarchy.getAllSuperclasses(type);
		for (IType superType : superTypes) {
			if (isJUnitTestCase(superType)) {
				return true;
			}
		}
		return false;
	}

	private boolean isJUnitTestCase(IType type) throws JavaModelException {
		String name = type.getFullyQualifiedName();
		return "junit.framework.TestCase".equals(name);
	}
}
