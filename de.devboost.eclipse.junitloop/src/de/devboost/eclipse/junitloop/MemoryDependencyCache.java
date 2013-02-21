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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * The {@link MemoryDependencyCache} can be used to cache dependencies that are
 * retrieved from another dependency provider in memory.
 */
public class MemoryDependencyCache implements IDependencyProvider {
	
	private IDependencyProvider delegate;
	private Map<String, Set<String>> cache = new LinkedHashMap<String, Set<String>>();
	
	/**
	 * Creates a new {@link MemoryDependencyCache} that retrieves dependencies
	 * from the given provider if they have not been retrieved before.
	 * 
	 * @param delegate the dependency provider to forward requests to if
	 *        dependency information is not present in the cache
	 */
	public MemoryDependencyCache(IDependencyProvider delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public Set<String> findDependencies(String path) {
		Set<String> deps = cache.get(path);
		if (deps == null) {
			deps = delegate.findDependencies(path);
			cache.put(path, deps);
		}
		return deps;
	}

	@Override
	public void clear(String path) {
		cache.remove(path);
	}
}
