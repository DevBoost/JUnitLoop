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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.jdt.junit.model.ITestElement.Result;
import org.eclipse.jdt.junit.model.ITestRunSession;

import de.devboost.eclipse.jloop.JLoopPlugin;

/**
 * The JUnitLoopTestRunListener makes sure that test which have failed will run
 * again. Also, tests that have succeeded, will be removed from the list of
 * scheduled tests. They will be added again by the 
 * {@link JUnitLoopCompilationParticipant} when a class that is referenced by
 * the test that has changed.
 */
public class JUnitLoopTestRunListener extends TestRunListener {
	
	private IJavaProject launchedProject;

	@Override
	public void sessionLaunched(ITestRunSession session) {
		super.sessionLaunched(session);
		launchedProject = session.getLaunchedProject();
		JUnitLoopPlugin.getDefault().notifySessionLaunched();
	}

	@Override
	public void sessionFinished(ITestRunSession session) {
		super.sessionFinished(session);
		launchedProject = null;
		JUnitLoopPlugin.getDefault().notifySessionFinished();
	}
	
	@Override
	public void testCaseFinished(ITestCaseElement testCaseElement) {
		super.testCaseFinished(testCaseElement);
		JUnitLoopPlugin plugin = JUnitLoopPlugin.getDefault();
		if (plugin == null) {
			return;
		}
		if (!plugin.isEnabled()) {
			return;
		}

		List<TestClass> failedTests = new ArrayList<TestClass>();
		List<TestClass> succeededTests = new ArrayList<TestClass>();

		String testClassName = testCaseElement.getTestClassName();
		if (testClassName.startsWith("junit.framework.TestSuite")) {
			return;
		}
		
		Result testResult = testCaseElement.getTestResult(true);
		TestClass testClass = createTestClass(testClassName);
		if (testClass == null) {
			return;
		}
		
		if (testResult == ITestElement.Result.FAILURE ||
			testResult == ITestElement.Result.ERROR) {
			failedTests.add(testClass);
		} else if (testResult == ITestElement.Result.OK) {
			succeededTests.add(testClass);
		}
		
		TestRunScheduler scheduler = new TestRunScheduler();
		scheduler.addFailedTests(failedTests);
		scheduler.addSucceededTests(succeededTests);
	}

	private TestClass createTestClass(String testClassName) {
		// create local copy to avoid potential multi-threading problems
		IJavaProject localProject = launchedProject;
		if (localProject != null) {
			try {
				IType type = localProject.findType(testClassName);
				IResource resource = type.getResource();
				String projectName = resource.getProject().getName();
				return new TestClass(projectName, type.getFullyQualifiedName());
			} catch (JavaModelException e) {
				JLoopPlugin.logError("Exception while determining Java type for test.", e);
			}
		}
		
		return null;
	}
}
