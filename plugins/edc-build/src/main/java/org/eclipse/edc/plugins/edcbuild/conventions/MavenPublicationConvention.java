/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Project;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Adds a Maven publication to a project.
 */
public class MavenPublicationConvention implements EdcConvention {

    /**
     * Default setting for publication of a project.
     */
    private static final boolean DEFAULT_SHOULD_PUBLISH = true;

    /**
     * Checks whether publishing is explicitly set to false for the target project and, if it is
     * not, adds a Maven publication to the project, if none exists. This only applies for
     * sub-projects that contain a build.gradle.kts file.
     *
     * @param target The project to which the convention applies
     */
    @Override
    public void apply(Project target) {
        // do not publish the root project or modules without a build.gradle.kts
        if (target.getRootProject() == target || !target.file("build.gradle.kts").exists()) {
            return;
        }

        var buildExt = requireExtension(target, BuildExtension.class);
        var shouldPublish = buildExt.getPublish().getOrElse(DEFAULT_SHOULD_PUBLISH);

        if (shouldPublish) {
            var pe = requireExtension(target, PublishingExtension.class);

            if (pe.getPublications().findByName(target.getName()) == null) {
                pe.publications(publications -> publications.create(target.getName(), MavenPublication.class,
                        mavenPublication -> {
                            mavenPublication.from(target.getComponents().getByName("java"));
                            mavenPublication.setGroupId(buildExt.getPom().getGroupId());
                            mavenPublication.suppressPomMetadataWarningsFor("testFixturesApiElements");
                            mavenPublication.suppressPomMetadataWarningsFor("testFixturesRuntimeElements");
                        }));
            }
        }
    }

}
