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

/**
 * The ChangeSkipManager is used to skip some changes reported by the 
 * {@link JUnitLoopCompilationParticipant}. This is required because generating
 * the JUnitLoop test suite triggers multiple builds. First, a build is 
 * triggered by the class path update, then another build is triggered when the 
 * loop test suite is updated. Without further precautions this would yield two 
 * subsequent JUnit runs which is neither intended nor useful.
 */
public class ChangeSkipManager {
	
	/**
	 * The number of changes to skip.
	 */
	private int skipCounter = 0;
	
	public synchronized void addSkip() {
		skipCounter++;
	}

	public synchronized boolean shallSkip() {
		boolean shallSkip = false;
		if (skipCounter > 0) {
			shallSkip = true;
		}
		skipCounter--;
		if (skipCounter < 0) {
			skipCounter = 0;
		}
		return shallSkip;
	}
}
