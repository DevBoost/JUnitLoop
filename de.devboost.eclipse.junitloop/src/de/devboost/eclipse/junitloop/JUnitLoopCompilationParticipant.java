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

import java.util.List;

import org.eclipse.core.resources.IResource;

import de.devboost.eclipse.jloop.AbstractLoopCompilationParticipant;

/**
 * The JUnitLoopCompilationParticipant is registered with the JDT to listen to
 * builds triggered on Java classes. Whenever a class is compiled, the 
 * {@link ChangeHandler} is informed, which takes appropriate actions.
 */
public class JUnitLoopCompilationParticipant extends AbstractLoopCompilationParticipant {
	
	@Override
	public void handleChange(List<IResource> files, boolean isBatch) {
		new ChangeHandler().handleChange(files, isBatch);
	}
}
