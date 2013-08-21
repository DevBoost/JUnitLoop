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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

public class JDTHelper {

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
