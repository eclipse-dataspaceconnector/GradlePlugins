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
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleVersionIdentifier;

import java.io.File;
import java.util.function.Supplier;

import static java.lang.String.format;

/**
 * Gradle plugin that injects an {@code annotationProcessor} dependency to any Gradle project so that the autodoc processor can run during compile.
 */
public class AutodocPlugin implements Plugin<Project> {

    private static final String PROCESSOR_ARTIFACT_NAME = "autodoc-processor";
    private static final String PLUGIN_ARTIFACT_NAME = "autodoc-plugin";
    private static final String GROUP_NAME = "org.eclipse.dataspaceconnector";

    @Override
    public void apply(Project project) {

        project.getExtensions().create("autodocextension", AutodocExtension.class);

        // adds the annotation processor dependency
        project.getGradle().addListener(new AutodocDependencyInjector(project, format("%s:%s:", GROUP_NAME, PROCESSOR_ARTIFACT_NAME),
                createVersionProvider(project),
                getOutputDirectoryProvider(project)));

        // registers a "named" task, that does nothing, except depend on the compileTask, which then runs the annotation processor
        project.getTasks().register("autodoc", t -> t.dependsOn("compileJava"));
        project.getTasks().register("mergeManifest", MergeManifestsTask.class, t -> t.dependsOn("autodoc"));

    }

    private Supplier<File> getOutputDirectoryProvider(Project project) {
        return () -> {
            var extension = project.getExtensions().findByType(AutodocExtension.class);
            if (extension != null) {
                var fileProvider = extension.getOutputDirectory();
                return fileProvider.isPresent() ? fileProvider.get() : null;
            }
            return null;
        };
    }

    private Supplier<String> createVersionProvider(Project project) {
        return () -> {
            // runtime version of the actual annotation processor, or override in config
            var versionToUse = getProcessorModuleVersion(project);

            var extension = project.getExtensions().findByType(AutodocExtension.class);
            if (extension != null && extension.getProcessorVersion().isPresent()) {
                versionToUse = extension.getProcessorVersion().get();
                project.getLogger().debug("{}: use configured version from AutodocExtension (override) [{}]", project.getName(), versionToUse);
            } else {
                project.getLogger().debug("{}: use default version [{}]", project.getName(), versionToUse);
            }
            return versionToUse;
        };
    }

    private String getProcessorModuleVersion(Project project) {
        Configuration classpath = project.getRootProject().getBuildscript().getConfigurations().getByName("classpath");
        return classpath.getResolvedConfiguration().getResolvedArtifacts().stream()
                .map(artifact -> artifact.getModuleVersion().getId())
                .filter(id -> GROUP_NAME.equals(id.getGroup()) && PLUGIN_ARTIFACT_NAME.equals(id.getName()))
                .findAny()
                .map(ModuleVersionIdentifier::getVersion)
                .orElse(null);
    }
}
