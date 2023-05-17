/*
 *  Copyright (c) 2022 - 2023 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.eclipse.edc.plugins.autodoc.AutodocExtension;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.eclipse.edc.plugins.edcbuild.extensions.MavenPomExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;

import java.nio.file.Path;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Configures the Maven POM for each project:
 * <ul>
 *     <li>sets project name, description, license, SCM info etc.</li>
 *     <li>adds an artifact for the documentation manifest ("edc.json")</li>
 * </ul>
 */
class MavenArtifactConvention implements EdcConvention {

    private static final String PROJECT_URL = "https://projects.eclipse.org/projects/technology.edc";

    @Override
    public void apply(Project target) {
        target.afterEvaluate(project -> {
            var pubExt = requireExtension(project, PublishingExtension.class);
            var pomExt = requireExtension(project, BuildExtension.class).getPom();

            pubExt.getPublications().stream()
                    .filter(p -> p instanceof MavenPublication)
                    .map(p -> (MavenPublication) p)
                    .peek(mavenPub -> mavenPub.pom(pom -> setPomInformation(pomExt, target, pom)))
                    .forEach(mavenPub -> addManifestArtifact(target, mavenPub));
        });
    }

    private Action<ConfigurablePublishArtifact> configureManifestArtifact() {
        return artifact -> {
            artifact.setClassifier("manifest");
            artifact.setType("json");
            artifact.builtBy("autodoc");
        };
    }

    private void addManifestArtifact(Project target, MavenPublication mavenPub) {
        var autodocExt = requireExtension(target, AutodocExtension.class);
        var pathToManifest = autodocExt.getOutputDirectory().getOrElse(target.getBuildDir()).getAbsolutePath();
        var manifestFileName = "edc.json";
        var manifestFile = Path.of(pathToManifest, manifestFileName).toFile();
        if (manifestFile.exists()) {
            var jsonArtifact = target.getArtifacts().add("archives", manifestFile, configureManifestArtifact());
            mavenPub.getArtifacts().artifact(jsonArtifact);
        }
    }

    private static void setPomInformation(MavenPomExtension pomExt, Project project, MavenPom pom) {
        // these properties are mandatory!
        var projectName = pomExt.getProjectName().getOrElse(project.getName());
        var description = pomExt.getDescription().getOrElse("edc :: " + project.getName());
        var projectUrl = pomExt.getProjectUrl().getOrElse(PROJECT_URL);
        pom.getName().set(projectName);
        pom.getDescription().set(description);
        pom.getUrl().set(projectUrl);

        // we'll provide a sane default for these properties
        pom.licenses(l -> l.license(pl -> {
            pl.getName().set(pomExt.getLicenseName().getOrElse("The Apache License, Version 2.0"));
            pl.getUrl().set(pomExt.getLicenseUrl().getOrElse("http://www.apache.org/licenses/LICENSE-2.0.txt"));
        }));

        pom.developers(d -> d.developer(md -> {
            md.getId().set(pomExt.getDeveloperId().getOrElse("mspiekermann"));
            md.getName().set(pomExt.getDeveloperName().getOrElse("Markus Spiekermann"));
            md.getEmail().set(pomExt.getDeveloperEmail().getOrElse("markus.spiekermann@isst.fraunhofer.de"));
        }));

        pom.scm(scm -> {
            scm.getUrl().set(pomExt.getScmUrl());
            scm.getConnection().set(pomExt.getScmConnection());
        });
    }

}
