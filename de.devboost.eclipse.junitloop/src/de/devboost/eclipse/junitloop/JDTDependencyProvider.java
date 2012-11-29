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
import java.util.Set;

import org.eclipse.core.runtime.CoreException;

import de.devboost.eclipse.jdtutilities.ClassDependencyUtility;
import de.devboost.eclipse.jloop.JLoopPlugin;

class JDTDependencyProvider implements IDependencyProvider {

	@Override
	public Set<String> findDependencies(String path) {
		try {
			ClassDependencyUtility util = new ClassDependencyUtility();
			return util.findDependencies(path);
		} catch (CoreException e) {
			JLoopPlugin.logError("Exception while searching for dependencies.", e);
			return Collections.emptySet();
		}
	}

	@Override
	public void clear(String path) {
		// do nothing
	}
}
