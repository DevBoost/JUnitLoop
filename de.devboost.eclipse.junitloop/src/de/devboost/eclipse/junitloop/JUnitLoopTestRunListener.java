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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.jdt.junit.model.ITestElement.FailureTrace;
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
	
	public static final String MARKER_TEST_FAILURE = "de.devboost.eclipse.junitloop.testFailure";
	
	private IJavaProject launchedProject;

	@Override
	public void sessionLaunched(ITestRunSession session) {
		super.sessionLaunched(session);
		launchedProject = session.getLaunchedProject();
		JUnitLoopPlugin.getDefault().notifySessionLaunched();
		
		// Delete all test failure markers
		try {
			ResourcesPlugin
					.getWorkspace()
					.getRoot()
					.deleteMarkers(MARKER_TEST_FAILURE, true,
							IResource.DEPTH_INFINITE);
		} catch (final CoreException e) {
			JLoopPlugin.logError("Couldn't delete old warnings", e);
		}
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
		
		// Add warning to Problems view
		if (testResult == ITestElement.Result.FAILURE ||
			testResult == ITestElement.Result.ERROR) {
			failedTests.add(testClass);
			writeMarkers(testCaseElement);
		} else if (testResult == ITestElement.Result.OK) {
			succeededTests.add(testClass);
		}
		
		TestRunScheduler scheduler = new TestRunScheduler();
		scheduler.addFailedTests(failedTests);
		scheduler.addSucceededTests(succeededTests);
	}
	
	private void writeMarkers(ITestCaseElement testCaseElement) {
		final String testClassName = testCaseElement.getTestClassName();
		final FailureTrace failureTrace = testCaseElement.getFailureTrace();
		final String trace = failureTrace.getTrace();
		String traceLine1 = null;
		for (final String traceLine : trace.split("[\\n\\r]+")) {
			if (traceLine1 == null) {
				traceLine1 = traceLine;
			}
			if (traceLine.indexOf(testClassName) > 0) {
				final String line = traceLine.substring(
						traceLine.indexOf(':') + 1, traceLine.length() - 1);
				try {
					final IType testClassType = launchedProject
							.findType(testClassName);
					final IResource resource = testClassType.getResource();
					
					final IMarker marker = resource
							.createMarker(MARKER_TEST_FAILURE);
					marker.setAttribute(IMarker.MESSAGE, traceLine1);
					marker.setAttribute(IMarker.SEVERITY,
							IMarker.SEVERITY_WARNING);
					marker.setAttribute(IMarker.LINE_NUMBER,
							Integer.parseInt(line));
					marker.setAttribute(IMarker.LOCATION, "line " + line);
					marker.setAttribute(IMarker.TRANSIENT, true);
					
					break;
				} catch (final Exception e) {
					JLoopPlugin.logError("Couldn't write problem marker", e);
				}
			}
		}
	}
	
	private TestClass createTestClass(String testClassName) {
		// create local copy to avoid potential multi-threading problems
		IJavaProject localProject = launchedProject;
		if (localProject != null) {
			try {
				IType type = localProject.findType(testClassName);
				if (type == null) {
					return null;
				}
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
