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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.osgi.framework.Bundle;

class RunInSameVMHandler implements IObjectLifecycleHandler {

	private final IType type;
	private Object newInstance;

	RunInSameVMHandler(IType type) {
		super();
		this.type = type;
	}

	@Override
	public void start() {
		newInstance = startNewInstanceInSameVM(type);
	}

	@Override
	public void stop() {
		killSafe(newInstance);
	}

	private Object startNewInstanceInSameVM(IType type) {
		IResource resource = type.getResource();
		IProject project = resource.getProject();
		OSGIHelper osgiHelper = new OSGIHelper();
		
		// reload referenced bundles
		try {
			IProject[] referencedProjects = project.getReferencedProjects();
			for (IProject referencedProject : referencedProjects) {
				reload(referencedProject, osgiHelper);
			}
			// TODO do this transitively?
		} catch (CoreException ce) {
			JLoopPlugin.logError("Can't reload required bundle.", ce);
		}
		
		// reload bundle itself
		Bundle bundle = reload(project, osgiHelper);
		if (bundle == null) {
			return null;
		}
		
		try {
			Class<?> loadedClass = bundle.loadClass(type.getFullyQualifiedName());
			Object newInstance = loadedClass.newInstance();
			invokeMethod(newInstance, IMagicMethodNames.RUN_IN_SAME_VM_METHOD_NAME);
			return newInstance;
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Bundle reload(IProject project, OSGIHelper osgiHelper) {
		String name = project.getName();
		// reload bundle(s)
		Bundle bundle = osgiHelper.installBundle(name);
		return bundle;
	}

	/**
	 * Calls the method with the given name on the given object.
	 */
	private Method invokeMethod(Object newInstance, String methodName)
			throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Method runMethod = newInstance.getClass().getMethod(methodName, new Class[0]);
		runMethod.invoke(newInstance, new Object[0]);
		return runMethod;
	}
	
	/**
	 * Calls the stop() method on the given object.
	 */
	private void kill(Object object) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (object == null) {
			return;
		}
		invokeMethod(object, IMagicMethodNames.STOP_METHOD_NAME);
	}
	
	private void killSafe(Object object) {
		try {
			kill(object);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}