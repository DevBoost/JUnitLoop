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
package de.devboost.eclipse.jloop.ui;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import de.devboost.eclipse.jloop.JLoopCompilationParticipant;
import de.devboost.eclipse.jloop.JLoopPlugin;

/**
 * The RunInLoopAction can be used to register Java files to run in a loop.
 * To work properly, these Java classes must have a public method run(), a
 * public method stop() and a constructor without arguments (may be implicit).
 * 
 * Once a Java class is selected to run in a loop, the 
 * {@link JLoopCompilationParticipant} will call the stop() method on previous
 * instances of the class, reload the bundle that contains the class, create
 * a new instance of the class and call the run() method.
 * 
 * This allows to immediately see the results of code changes (e.g., when
 * developing a GUI) as the {@link JLoopCompilationParticipant} is triggered
 * whenever a class is compiled in Eclipse.
 * 
 * TODO move 'Stop class from running loop command' to end of 'Run' menu
 */
public class RunInLoopAction implements IObjectActionDelegate {

	private ISelection selection;

	@Override
	public void run(IAction action) {
		if (selection instanceof IStructuredSelection) {
			Iterator<?> it = ((IStructuredSelection) selection).iterator();
            while (it.hasNext()) {
                Object o = it.next();
                if (o instanceof IFile) {
                	IFile resource = (IFile) o;                   
					setFileToRunInLoop(resource);
                }
            }
		}
	}

	private void setFileToRunInLoop(IFile file) {
		JLoopPlugin.getDefault().setLoopFile(file);
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;	
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		// do nothing
	}
}
