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
package de.devboost.eclipse.junitloop.launch;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

import de.devboost.eclipse.jloop.AbstractLaunchProjectUpdater;
import de.devboost.eclipse.junitloop.TestRunScheduler;

public class TestSuiteProjectUpdater extends AbstractLaunchProjectUpdater {

	public TestSuiteProjectUpdater() {
		super(new TestSuiteProjectData());
	}

	private Set<String> testsToRun;
	
	public void updateLoopTestSuite() {
		synchronized (TestSuiteProjectUpdater.class) {
			TestRunScheduler scheduler = new TestRunScheduler();
			this.testsToRun = scheduler.getTestsToRun();
			Set<String> testProjects = scheduler.getTestProjects();
			updateLaunchProject(testProjects);
		}
	}

	@Override
	protected String getLongProjectName() {
		return "JUnitLoop test suite";
	}

	@Override
	protected String getSourceCode() {
		StringBuilder classes = new StringBuilder();
		for (String testToRun : testsToRun) {
			if ("".equals(testToRun.trim())) {
				continue;
			}
			classes.append(testToRun);
			classes.append(".class, ");
		}
		String code = 
			"import org.junit.runner.RunWith;\n" +
			"import org.junit.runners.Suite;\n" +
			"import org.junit.runners.Suite.SuiteClasses;\n" +
			"\n" +
			"/** This class is generated and will be overridden. */\n" +
			"@RunWith(Suite.class)\n" +
			"@SuiteClasses({" + classes + "})\n" + 
			"public class LoopTestSuite {\n" +
			// we use a time stamp to make sure this class is compiled after
			// generating it.
			"\n" +
			"\tpublic void test" + System.currentTimeMillis() + "() {}\n" + 
			"}";
		return code;
	}

	@Override
	protected Collection<IClasspathEntry> getAdditionalClassPathEntries() {
		IPath path = new Path("org.eclipse.jdt.junit.JUNIT_CONTAINER/4");
		IClasspathEntry junitEntry = JavaCore.newContainerEntry(path);
		return Collections.singleton(junitEntry);
	}
}
