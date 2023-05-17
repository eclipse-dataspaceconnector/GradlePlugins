/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.autodoc;

import org.eclipse.edc.plugins.autodoc.merge.MergeManifestsTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;

/**
 * Gradle plugin that injects an {@code annotationProcessor} dependency to any Gradle project so that the autodoc processor can run during compile.
 */
public class AutodocPlugin implements Plugin<Project> {

    private final List<String> exclusions = List.of("runtime-metamodel", "version-catalog", "edc-build", "module-names", "openapi-merger", "test-summary", "autodoc-plugin", "autodoc-processor");

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create("autodocextension", AutodocExtension.class);

        if (!exclusions.contains(project.getName())) {
            project.getGradle().addListener(new AutodocDependencyInjector(project, extension));
        }

        // registers a "named" task, that does nothing, except depend on the compileTask, which then runs the annotation processor
        project.getTasks().register("autodoc", t -> t.dependsOn("compileJava"));
        project.getTasks().register("mergeManifest", MergeManifestsTask.class, t -> t.dependsOn("autodoc"));

    }

}
