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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CompilationParticipant;

public abstract class AbstractLoopCompilationParticipant extends CompilationParticipant {
	
	private BuildContext[] buildContexts;
	private boolean isBatch;

	@Override
	public void buildStarting(BuildContext[] buildContexts, boolean isBatch) {
		super.buildStarting(buildContexts, isBatch);
		
		this.buildContexts = buildContexts;
		this.isBatch = isBatch;
	}

	@Override
	public void buildFinished(IJavaProject project) {
		super.buildFinished(project);
		
		BuildContext[] contexts = buildContexts;
		if (contexts == null) {
			return;
		}
		buildContexts = null;

		List<IResource> files = new ArrayList<IResource>();
		for (BuildContext context : contexts) {
			IFile file = context.getFile();
			files.add(file);
		}

		handleChange(files, isBatch);
	}
	
	@Override
	public boolean isActive(IJavaProject project) {
		return true;
	}

	protected abstract void handleChange(List<IResource> files, boolean isBatch);
}
