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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;

import de.devboost.eclipse.jdtutilities.CompilationEvent;
import de.devboost.eclipse.jloop.launch.JLoopLaunchProjectData;

/**
 * The JLoopCompilationParticipant runs all Java classes that are registered to
 * run in a loop when compiling Java files is finished. 
 */
public class JLoopCompilationParticipant extends AbstractLoopCompilationParticipant {
	
	@Override
	public void handleChange(Collection<CompilationEvent> events) {
		boolean compiledLoopWrapper = false;
		Set<String> currentProjects = new LinkedHashSet<String>();
		String sourcePath = new JLoopLaunchProjectData().getSourcePath();
		for (CompilationEvent event : events) {
			// TODO is this correct?
			if (event.isBatch()) {
				continue;
			}
			IFile file = event.getContext().getFile();
			if (file.getFullPath().toString().equals(sourcePath)) {
				compiledLoopWrapper = true;
			} else {
				currentProjects.add(file.getProject().getName());
			}
		}
		
		if (compiledLoopWrapper) {
			new LoopRunner().runLoopFiles();
		} else {
			new LoopRunner().updateLaunchProject(currentProjects);
		}
	}
}
