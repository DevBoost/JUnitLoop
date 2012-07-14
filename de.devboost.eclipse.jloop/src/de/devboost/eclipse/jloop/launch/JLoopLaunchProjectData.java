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
package de.devboost.eclipse.jloop.launch;

import de.devboost.eclipse.jloop.AbstractLaunchProjectData;

public class JLoopLaunchProjectData extends AbstractLaunchProjectData {
	
	private static final String MAIN_CLASS = "LoopLaunchWrapper";
	
	@Override
	public String getProjectName() {
		return "JLoop";
	}

	@Override
	public String getMainClassName() {
		return MAIN_CLASS;
	}
}
