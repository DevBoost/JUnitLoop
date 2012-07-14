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
package de.devboost.eclipse.junitloop.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import de.devboost.eclipse.junitloop.JUnitLoopPlugin;

// TODO save the state of the action to preserve it when Eclipse is shut down
public class EnableAction implements IViewActionDelegate {

	@Override
	public void run(IAction action) {
		JUnitLoopPlugin.getDefault().setEnabled(action.isEnabled());
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}

	@Override
	public void init(IViewPart view) {
		// do nothing
	}
}
