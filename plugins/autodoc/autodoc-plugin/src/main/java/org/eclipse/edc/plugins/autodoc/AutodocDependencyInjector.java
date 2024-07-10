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

import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencyResolutionListener;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.List;

import static java.lang.String.format;

/**
 * Adds an {@code annotationProcessor("...")} dependency to the project
 */
class AutodocDependencyInjector implements DependencyResolutionListener {
    private static final String ANNOTATION_PROCESSOR = "annotationProcessor";
    private static final String VERSION = "edc.version"; // must be identical to EdcModuleProcessor.VERSION
    private static final String ID = "edc.id"; // must be identical to EdcModuleProcessor.ID
    private static final String OUTPUTDIR = "edc.outputDir"; // must be identical to EdcModuleProcessor.EDC_OUTPUTDIR_OVERRIDE
    private static final String DEPENDENCY_NAME = format("%s:%s", "org.eclipse.edc", "autodoc-processor");
    private final Project project;
    private final AutodocExtension extension;

    AutodocDependencyInjector(Project project, AutodocExtension extension) {
        this.project = project;
        this.extension = extension;
    }

    @Override
    public void beforeResolve(ResolvableDependencies dependencies) {
        var processorVersion = extension.getProcessorVersion();

        var artifact = DEPENDENCY_NAME;
        if (processorVersion.isPresent()) {
            var version = processorVersion.get();
            artifact += ":" + version;
            project.getLogger().debug("{}: use configured version from AutodocExtension (override) [{}]", project.getName(), version);
        } else {
            artifact += ":+";
            project.getLogger().info("No explicit configuration value for the annotationProcessor version was found. Current one will be used");
        }

        if (addDependency(project, artifact)) {
            var task = project.getTasks().findByName("compileJava");
            if ((task instanceof JavaCompile compileJava)) {
                var versionArg = format("-A%s=%s", VERSION, project.getVersion());
                var idArg = format("-A%s=%s:%s", ID, project.getGroup(), project.getName());
                var outputArg = format("-A%s=%s", OUTPUTDIR, extension.getOutputDirectory().getOrNull());

                compileJava.getOptions().getCompilerArgs().addAll(List.of(idArg, versionArg, outputArg));
            }
        }
        project.getGradle().removeListener(this);
    }

    @Override
    public void afterResolve(ResolvableDependencies dependencies) {

    }

    /**
     * Adds an {@code annotationProcessor} dependency to the given project.
     *
     * @param project        The Gradle project, to which the annotationProcessor dep is to be added
     * @param dependencyName The fully qualified maven coordinates (GROUPID:ARTIFACT:VERSION) of te dependency
     * @return true if the dependency was added successfully, false if the project does not have an {@code annotationProcessor} configuration, or the dep could not be added.
     */
    private boolean addDependency(Project project, String dependencyName) {
        var apConfig = project.getConfigurations().findByName(ANNOTATION_PROCESSOR);
        if (apConfig != null) {
            project.getLogger().debug("autodoc: Add dependency {}(\"{}\") to project {}", ANNOTATION_PROCESSOR, dependencyName, project.getName());
            return apConfig.getDependencies().add(project.getDependencies().create(dependencyName));
        }
        return false;
    }
}
