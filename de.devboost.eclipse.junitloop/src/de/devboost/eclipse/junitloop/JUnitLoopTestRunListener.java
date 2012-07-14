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

import org.eclipse.jdt.junit.TestRunListener;
import org.eclipse.jdt.junit.model.ITestCaseElement;
import org.eclipse.jdt.junit.model.ITestElement;
import org.eclipse.jdt.junit.model.ITestRunSession;
import org.eclipse.jdt.junit.model.ITestElement.Result;

/**
 * The JUnitLoopTestRunListener makes sure that test which have failed will run
 * again. Also, tests that have succeeded, will be removed from the list of
 * scheduled tests. They will be added again by the 
 * {@link JUnitLoopCompilationParticipant} when a class that is referenced by
 * the test that has changed.
 */
public class JUnitLoopTestRunListener extends TestRunListener {
	
	@Override
	public void sessionLaunched(ITestRunSession session) {
		super.sessionLaunched(session);
		JUnitLoopPlugin.getDefault().notifySessionLaunched();
	}

	@Override
	public void sessionFinished(ITestRunSession session) {
		super.sessionFinished(session);
		JUnitLoopPlugin.getDefault().notifySessionFinished();
	}
	
	@Override
	public void testCaseFinished(ITestCaseElement testCaseElement) {
		super.testCaseFinished(testCaseElement);
		if (!JUnitLoopPlugin.getDefault().isEnabled()) {
			return;
		}

		List<String> failedTests = new ArrayList<String>();
		List<String> succeededTests = new ArrayList<String>();

		String testClassName = testCaseElement.getTestClassName();
		if (testClassName.startsWith("junit.framework.TestSuite")) {
			return;
		}
		Result testResult = testCaseElement.getTestResult(true);
		if (testResult == ITestElement.Result.FAILURE ||
			testResult == ITestElement.Result.ERROR) {
			failedTests.add(testClassName);
		} else if (testResult == ITestElement.Result.OK) {
			succeededTests.add(testClassName);
		}
		
		TestRunScheduler scheduler = new TestRunScheduler();
		scheduler.addFailedTests(failedTests);
		scheduler.addSucceededTests(succeededTests);
	}
}
