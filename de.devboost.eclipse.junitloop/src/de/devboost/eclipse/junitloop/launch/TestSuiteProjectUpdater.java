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
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

import de.devboost.eclipse.jloop.AbstractLaunchProjectUpdater;
import de.devboost.eclipse.junitloop.TestClass;
import de.devboost.eclipse.junitloop.TestRunScheduler;

public class TestSuiteProjectUpdater extends AbstractLaunchProjectUpdater {

	public static final String LOOP_TEST_SUITE_NAME = "LoopTestSuite";

	public TestSuiteProjectUpdater() {
		super(new TestSuiteProjectData());
	}

	private Set<String> testsToRun;
	
	public void updateLoopTestSuite() {
		synchronized (TestSuiteProjectUpdater.class) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();

			TestRunScheduler scheduler = new TestRunScheduler();
			this.testsToRun = new LinkedHashSet<String>();

			Set<String> requiredProjects = new LinkedHashSet<String>(); 
			Set<TestClass> scheduledTests = scheduler.getTestsToRun();
			for (TestClass scheduledTest : scheduledTests) {
				// exclude tests that are located in closed projects
				String projectName = scheduledTest.getContainingProject();
				IProject project = root.getProject(projectName);
				if (project.isOpen()) {
					this.testsToRun.add(scheduledTest.getQualifiedClassName());
					requiredProjects.add(projectName);
				}
			}
			
			updateLaunchProject(requiredProjects);
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
			if (LOOP_TEST_SUITE_NAME.equals(testToRun.trim())) {
				continue;
			}
			classes.append(testToRun);
			classes.append(".class, ");
		}
		// TODO check whether the Categories test runner works with older
		// JUnit versions (e.g., the one bundled with Eclipse 3.7). If not,
		// we must use the normal runner (Suite.class).
		String code = 
			"/** This class is generated and will be overridden. */\n" +
			"@org.junit.runner.RunWith(org.junit.experimental.categories.Categories.class)\n" +
			//"@org.junit.runner.RunWith(org.junit.runners.Suite.class)\n" +
			"@org.junit.runners.Suite.SuiteClasses({" + classes + "})\n" + 
			"@org.junit.experimental.categories.Categories.ExcludeCategory(Runtime.class)\n" +
			//"@org.junit.experimental.categories.Categories.ExcludeCategory(" + SlowTests.class.getName() + ".class)\n" +
			"public class " + LOOP_TEST_SUITE_NAME + " {\n" +
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

	/**
	 * Returns true if there are tests to run. Otherwise false is returned as 
	 * there is no need to update (and execute) the test suite if there are no 
	 * tests to run anyway. Running an empty test suite is just confusing. 
	 */
	@Override
	protected boolean updateRequired() {
		return !testsToRun.isEmpty();
	}
}
