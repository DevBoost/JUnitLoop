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

import de.devboost.buildboost.buildext.test.junit.stages.JUnitTestStage;
import de.devboost.buildboost.filters.IdentifierRegexFilter;
import de.devboost.buildboost.genext.updatesite.stages.BuildUpdateSiteStage;
import de.devboost.buildboost.model.IBuildConfiguration;
import de.devboost.buildboost.model.IBuildParticipant;
import de.devboost.buildboost.model.IBuildStage;
import de.devboost.buildboost.stages.CompileStage;
import de.devboost.buildboost.stages.CopyProjectsStage;

public class BuildScriptGenerator implements IBuildConfiguration {

	@Override
	public List<IBuildStage> getBuildStages(String workspace) {
		
		String buildDir = workspace + File.separator + "build";
		//String testResultDir = buildDir + File.separator + "test-results";
		//String eclipseHome = buildDir + File.separator + "target-platform";
		String eclipsePluginBuildDir = buildDir + File.separator + "eclipse-plugins";

		CopyProjectsStage stage1 = new CopyProjectsStage();
		stage1.setReposFolder(workspace);
		stage1.setArtifactsFolder(eclipsePluginBuildDir);
		stage1.addBuildParticipants(getAdditionalParticipants());
		
		CompileStage stage2 = new CompileStage();
		stage2.setArtifactsFolder(eclipsePluginBuildDir);
		//stage2.setEclipseHome(eclipseHome);
		stage2.setSourceFileEncoding("utf-8");
		stage2.addBuildParticipants(getAdditionalParticipants());
		
		JUnitTestStage stage3 = new JUnitTestStage();
		stage3.setArtifactsFolder(eclipsePluginBuildDir);
		//stage3.setTestResultPath(testResultDir);
		//stage3.setEclipseHome(eclipseHome);

		BuildUpdateSiteStage stage4 = new BuildUpdateSiteStage();
		stage4.setArtifactsFolder(buildDir);
		//stage4.setEclipseHome(eclipseHome);
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
