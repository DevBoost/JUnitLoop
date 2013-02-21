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

import java.util.Set;

/**
 * An {@link IDependencyProvider} can be used to find dependencies between Java
 * classes.
 */
public interface IDependencyProvider {

	/**
	 * Determines all classes which a Java class depends on directly.
	 * 
	 * @param path the workspace path of the Java class
	 * 
	 * @return a set of fully qualifies class names the given class depends on
	 */
	public Set<String> findDependencies(String path);

	/**
	 * Clears the dependency information for the class located at the given 
	 * path. This is required in case the {@link IDependencyProvider} at hand
	 * has some kind of caching mechanism.
	 */
	public void clear(String path);
}
