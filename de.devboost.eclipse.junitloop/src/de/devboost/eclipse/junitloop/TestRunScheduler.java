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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.devboost.eclipse.jloop.JLoopPlugin;
import de.devboost.eclipse.junitloop.launch.TestSuiteProjectUpdater;

/**
 * The TestRunScheduler can be used to schedule tests for the next run of the
 * JUnitLoop test suite. Usually, tests that have failed or that have changed 
 * are scheduled to run again, while test that have succeeded are removed from
 * the schedule.
 * 
 * The TestRunScheduler uses plain test files that are stored in the JUnitLoop
 * project folder to persist this information.
 */
public class TestRunScheduler {
	
	private static final String LINE_DELIMITER = "\n";
	private static final String TEST_PROJECTS_FILE = "test_projects";
	private static final String TESTS_TO_RUN_FILE = "tests_to_run";

	public void addFailedTests(Collection<String> failedTests) {
		StringBuilder text = getText(failedTests);
		appendContent(TESTS_TO_RUN_FILE, text);
	}

	public void addSucceededTests(Collection<String> succeededTests) {
		Set<String> testsToRun = getTestsToRun();
		testsToRun.removeAll(succeededTests);
		StringBuilder text = getText(testsToRun);
		setContent(TESTS_TO_RUN_FILE, text, false);
	}

	public void addTestProjects(Collection<String> testProjects) {
		StringBuilder text = getText(testProjects);
		appendContent(TEST_PROJECTS_FILE, text);
	}

	private StringBuilder getText(Collection<String> tests) {
		StringBuilder text = new StringBuilder();
		for (String test : tests) {
			text.append(test);
			text.append(LINE_DELIMITER);
		}
		return text;
	}
	
	public Set<String> getTestsToRun() {
		return getLines(TESTS_TO_RUN_FILE);
	}

	public Set<String> getTestProjects() {
		return getLines(TEST_PROJECTS_FILE);
	}

	private Set<String> getLines(String filename) {
		IProject loopSuiteProject = new TestSuiteProjectUpdater().getProject();
		IFile file = loopSuiteProject.getFile(filename);
		if (!file.exists()) {
			return Collections.emptySet();
		}
		
		try {
			InputStream stream = file.getContents();
			String content = getContentAsString(stream);
			if (content.trim().isEmpty()) {
				return Collections.emptySet();
			}
			String[] lines = content.split(LINE_DELIMITER);
			return new LinkedHashSet<String>(Arrays.asList(lines));
		} catch (CoreException e) {
			return Collections.emptySet();
		} catch (IOException e) {
			return Collections.emptySet();
		}
	}

	private void appendContent(String filename, StringBuilder text) {
		setContent(filename, text, true);
	}

	private void setContent(String filename, StringBuilder text, boolean append) {
		IProject loopSuiteProject = new TestSuiteProjectUpdater().getProject();
		IFile file = loopSuiteProject.getFile(filename);
		InputStream stream = new ByteArrayInputStream(text.toString().getBytes());
		try {
			if (file.exists()) {
				if (append) {
					file.appendContents(stream, true, false, new NullProgressMonitor());
				} else {
					file.setContents(stream, true, false, new NullProgressMonitor());
				}
			} else {
				file.create(stream, true, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			JLoopPlugin.logError("Can't write test scheduled for execution in loop.", e);
		}
	}

	private String getContentAsString(InputStream inputStream) throws IOException {
		StringBuffer content = new StringBuffer();
		InputStreamReader reader = new InputStreamReader(inputStream);
		int next = -1;
		while ((next = reader.read()) >= 0) {
			content.append((char) next);
		}
		inputStream.close();
		return content.toString();
	}
}
