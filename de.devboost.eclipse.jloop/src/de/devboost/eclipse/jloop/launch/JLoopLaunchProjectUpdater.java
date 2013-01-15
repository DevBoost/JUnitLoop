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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IType;

import de.devboost.eclipse.jloop.AbstractLaunchProjectUpdater;
import de.devboost.eclipse.jloop.IMagicMethodNames;
import de.devboost.eclipse.jloop.JDTHelper;

/**
 * The JLoopLaunchProjectUpdater takes care of updating the launch project that
 * is generated in order to launch the class that runs in a loop. This mainly
 * required to create a class that calls the run() method on the class in the
 * loop and the stop() method when this is demanded by the life cycle handler
 * (e.g. when the class was modified and needs to be reloaded).
 */
public class JLoopLaunchProjectUpdater extends AbstractLaunchProjectUpdater {

	private String loopClassName;
	private boolean hasStopMethod;
	private boolean isRunInNewVM;
	
	public JLoopLaunchProjectUpdater(IType type, boolean isRunInNewVM) {
		super(new JLoopLaunchProjectData());
		this.isRunInNewVM = isRunInNewVM;
		this.loopClassName = type.getFullyQualifiedName();
		this.hasStopMethod = new JDTHelper().hasStopMethod(type);
	}
	
	@Override
	protected Collection<IClasspathEntry> getAdditionalClassPathEntries() {
		return Collections.emptyList();
	}

	@Override
	protected String getSourceCode() {
		String mainClassName = getProjectData().getMainClassName();
		String code;
		if (isRunInNewVM) {
			code = getRunInNewVMCode(mainClassName);
		} else {
			code = getRunInSameVMCode(mainClassName);
		}
		return code;
	}

	private String getRunInSameVMCode(String mainClassName) {
		// we use a time stamp to make sure this class is compiled after
		// generating it.
		long timeStamp = System.currentTimeMillis();

		String code = 
				"/** This class is generated and will be overridden. */\n" +
				"public class " + mainClassName + " {\n" +
				"\n" +
				"\t/** The class that currently runs in loop, runs in the same VM. Thus, this class is not used. */\n" +
				"\tpublic void notUsed" + timeStamp + "() {\n" + 
				"\t}\n" +
				"}";
		return code;
	}

	private String getRunInNewVMCode(String mainClassName) {
		// we use a time stamp to make sure this class is compiled after
		// generating it.
		long timeStamp = System.currentTimeMillis();
		
		String code = 
			"/** This class is generated and will be overridden. */\n" +
			"public class " + mainClassName + " {\n" +
			"\n" +
			"\tprivate " + loopClassName + " instance;\n" +
			"\n" +
			"\tpublic static void main(String[] args) {\n" + 
			(hasStopMethod ? 
				"\t\tnew " + mainClassName + "().start" + timeStamp + "(args[0]);\n" :
				"\t\tnew " + mainClassName + "().start" + timeStamp + "(null);\n"
			) +
			"\t}\n" +
			"\n" +
			"\tpublic void start" + timeStamp + "(String stopFile) {\n" + 
			"\t\tinstance = new " + loopClassName + "();\n";
		if (hasStopMethod) {
			code += "\t\tstartWaitForStopThread(stopFile);\n";
		}
		code += "\t\tinstance." + IMagicMethodNames.RUN_IN_NEW_VM_METHOD_NAME + "();\n" +
			"\t}\n" +
			"\n";
		if (hasStopMethod) {
			code += "\tprivate void startWaitForStopThread(final String stopFile) {\n" +
			"\t\tif (stopFile == null) {\n" +
			"\t\t\treturn;\n" +
			"\t\t}\n" +
			"\t\tThread waitForStopThread = new Thread(new Runnable() {\n" +
			"\t\t\tpublic void run() {\n" +
			"\t\t\t\ttry {\n" +
			"\t\t\t\t\twhile (true) {\n" +
			"\t\t\t\t\t\tjava.io.InputStream inputStream = new java.io.FileInputStream(new java.io.File(stopFile));\n" +
			"\t\t\t\t\t\tfinal java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));\n" +
			"\t\t\t\t\t\tString answer = reader.readLine();\n" +
			"\t\t\t\t\t\treader.close();\n" +
			"\t\t\t\t\t\tif (\"" + IMagicMethodNames.STOP_METHOD_NAME + "\".equals(answer)) {\n" +
			"\t\t\t\t\t\t\tinstance." + IMagicMethodNames.STOP_METHOD_NAME + "();\n" +
			"\t\t\t\t\t\t\tbreak;\n" +
			"\t\t\t\t\t\t}\n" +
			"\t\t\t\t\t\tThread.sleep(100);\n" +
			"\t\t\t\t\t}\n" +
			"\t\t\t\t} catch (java.io.IOException e) {\n" +
			"\t\t\t\t\te.printStackTrace();\n" +
			"\t\t\t\t} catch (InterruptedException e) {\n" +
			"\t\t\t\t\te.printStackTrace();\n" +
			"\t\t\t\t}\n" +
			"\t\t\t}\n" +
			"\t\t});\n" +
			"\t\twaitForStopThread.start();\n" +
			"\t}\n";
		}
		code += "}";
		return code;
	}

	@Override
	protected String getLongProjectName() {
		return "JLoop launch";
	}

	/**
	 * Returns always true as there is no situation where the JLoop launch
	 * project must not be updated. 
	 */
	@Override
	protected boolean updateRequired() {
		return true;
	}
}
