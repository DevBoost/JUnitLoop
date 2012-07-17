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
package de.devboost.eclipse.junitloop.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.dropsbox.autobuild.filters.IdentifierRegexFilter;
import org.dropsbox.autobuild.model.IBuildConfiguration;
import org.dropsbox.autobuild.model.IBuildParticipant;
import org.dropsbox.autobuild.model.IBuildStage;
import org.dropsbox.autobuild.stages.BuildUpdateSiteStage;
import org.dropsbox.autobuild.stages.CompileStage;
import org.dropsbox.autobuild.stages.CopyProjectsStage;
import org.dropsbox.autobuild.stages.JUnitTestStage;

public class BuildScriptGenerator implements IBuildConfiguration {

	@Override
	public List<IBuildStage> getBuildStages(String workspace) {
		
		String buildDir = workspace + File.separator + "build";
		String testResultDir = buildDir + File.separator + "test-results";
		String eclipseHome = buildDir + File.separator + "target-platform";
		String eclipsePluginBuildDir = buildDir + File.separator + "eclipse-plugins";

		CopyProjectsStage stage1 = new CopyProjectsStage();
		stage1.setSourcePath(workspace);
		stage1.setTargetPath(eclipsePluginBuildDir);
		stage1.addBuildParticipants(getAdditionalParticipants());
		
		CompileStage stage2 = new CompileStage();
		stage2.setBuildDirPath(eclipsePluginBuildDir);
		stage2.setEclipseHome(eclipseHome);
		stage2.setSourceFileEncoding("utf-8");
		stage2.addBuildParticipants(getAdditionalParticipants());
		
		JUnitTestStage stage3 = new JUnitTestStage();
		stage3.setBuildDirPath(eclipsePluginBuildDir);
		stage3.setTestResultPath(testResultDir);
		stage3.setEclipseHome(eclipseHome);

		BuildUpdateSiteStage stage4 = new BuildUpdateSiteStage();
		stage4.setBuildDirPath(buildDir);
		stage4.setEclipseHome(eclipseHome);
		stage4.setUsernameProperty("user-devboost-jenkins");
		stage4.setPasswordProperty("pass-devboost-jenkins");

		List<IBuildStage> stages = new ArrayList<IBuildStage>();
		stages.add(stage1);
		stages.add(stage2);
		stages.add(stage3);
		stages.add(stage4);
		
		return stages;
	}

	private List<IBuildParticipant> getAdditionalParticipants() {
		List<IBuildParticipant> participants = new ArrayList<IBuildParticipant>();
		participants.add(new IdentifierRegexFilter(".*jloop.*").or(new IdentifierRegexFilter(".*junitloop.*")));
		return participants;
	}
}
