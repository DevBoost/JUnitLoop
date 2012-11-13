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

import de.devboost.eclipse.jdtutilites.AbstractCompilationParticipant;
import de.devboost.eclipse.jdtutilites.CompilationEvent;

public abstract class AbstractLoopCompilationParticipant extends AbstractCompilationParticipant {

	@Override
	public void buildStarting(CompilationEvent event) {
		// do nothing	
	}

	@Override
	public void buildFinished(Collection<CompilationEvent> events) {
		handleChange(events);
	}
	
	protected abstract void handleChange(Collection<CompilationEvent> events);
}
