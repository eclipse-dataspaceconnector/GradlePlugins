/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.autodoc;

import org.eclipse.edc.plugins.autodoc.tasks.ManifestDownloadTask;
import org.eclipse.edc.plugins.autodoc.tasks.MarkdownRendererTask.ToHtml;
import org.eclipse.edc.plugins.autodoc.tasks.MarkdownRendererTask.ToMarkdown;
import org.eclipse.edc.plugins.autodoc.tasks.MergeManifestsTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.util.List;

/**
 * Gradle plugin that injects an {@code annotationProcessor} dependency to any Gradle project so that the autodoc processor can run during compile.
 */
public class AutodocPlugin implements Plugin<Project> {

    public static final String GROUP_NAME = "autodoc";
    public static final String AUTODOC_TASK_NAME = "autodoc";
    private final List<String> exclusions = List.of("version-catalog", "edc-build", "module-names", "openapi-merger", "test-summary", "autodoc-plugin", "autodoc-processor", "autodoc-converters");

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions().create("autodocextension", AutodocExtension.class);

        if (!exclusions.contains(project.getName())) {
            project.getGradle().addListener(new AutodocDependencyInjector(project, extension));
        }

        project.getTasks().register(AUTODOC_TASK_NAME, t -> t.dependsOn("compileJava").setGroup(GROUP_NAME));
        project.getTasks().register(MergeManifestsTask.NAME, MergeManifestsTask.class, t -> t.dependsOn(AUTODOC_TASK_NAME).setGroup(GROUP_NAME));
        project.getTasks().register(ToMarkdown.NAME, ToMarkdown.class, t -> t.setGroup(GROUP_NAME));
        project.getTasks().register(ToHtml.NAME, ToHtml.class, t -> t.setGroup(GROUP_NAME));
        project.getTasks().register(ManifestDownloadTask.NAME, ManifestDownloadTask.class, t -> t.setGroup(GROUP_NAME));
    }
}
