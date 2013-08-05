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
package de.devboost.eclipse.jloop;

import java.io.PrintStream;
import java.lang.reflect.Field;
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
			
			// Set output stream writer (if available)
			try {
				Field outField = newInstance.getClass().getField("out");
				Class<?> fieldType = outField.getType();
				if (PrintStream.class.getName().equals(fieldType.getName())) {
					PrintStream consolePrintStream = new ConsolePrintStream();
					outField.set(newInstance, consolePrintStream);
				}
			} catch (NoSuchFieldException nsfe) {
				// Ignore
			}
			// Invoke method runInSameVM()
			try {
				Method runMethod = newInstance.getClass().getMethod(
						IMagicMethodNames.RUN_IN_SAME_VM_METHOD_NAME, String.class);
				runMethod.invoke(newInstance, JLoopPlugin.getDefault().getLoopFile().getFullPath().toString());
			} catch (NoSuchMethodException e) {
				invokeMethod(newInstance, IMagicMethodNames.RUN_IN_SAME_VM_METHOD_NAME);
			}
			return newInstance;
		} catch (ClassNotFoundException e) {
			printAndLog(e);
		} catch (Error e) {
			printAndLog(e);
		} catch (IllegalArgumentException e) {
			printAndLog(e);
		} catch (IllegalAccessException e) {
			printAndLog(e);
		} catch (InvocationTargetException e) {
			printAndLog(e);
		} catch (SecurityException e) {
			printAndLog(e);
		} catch (NoSuchMethodException e) {
			printAndLog(e);
		} catch (InstantiationException e) {
			printAndLog(e);
		}
		return null;
	}

	private void printAndLog(Throwable e) {
		e.printStackTrace();
		JLoopPlugin.logError("Can't run class in loop.", e);
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
		Method method = newInstance.getClass().getMethod(methodName, new Class[0]);
		method.invoke(newInstance, new Object[0]);
		return method;
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
			// Ignore this
		} catch (IllegalAccessException e) {
			// Ignore this
		} catch (InvocationTargetException e) {
			// Ignore this
		}
	}
}