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

package org.eclipse.edc.plugins.edcbuild.conventions;

import io.swagger.v3.plugins.gradle.tasks.ResolveTask;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerResolveConventionTest {

    private static final String PROJECT_NAME = "testproject";
    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().withName(PROJECT_NAME).build();
        project.getPluginManager().apply("io.swagger.core.v3.swagger-gradle-plugin");
        project.getPluginManager().apply(JavaPlugin.class);
        project.getExtensions().create("edcBuild", BuildExtension.class, project.getObjects());
    }

    @Test
    void apply_whenApiGroupNotSpecified_shouldUseDefault() {
        var convention = new SwaggerResolveConvention();
        convention.apply(project);

        var resolveTask = (ResolveTask) project.getTasks().getByName("resolve");

        assertThat(resolveTask.getOutputDir().getPath()).endsWith("/resources/openapi/yaml");
        assertThat(resolveTask.getOutputFileName()).isEqualTo(PROJECT_NAME);
        assertThat(resolveTask.getOutputFormat()).isEqualTo(ResolveTask.Format.YAML);
    }

    @Test
    void apply_whenApiGroupSpecified_shouldAppend() {
        var swagger = ConventionFunctions.requireExtension(project, BuildExtension.class).getSwagger();
        swagger.getApiGroup().set("test-api");
        var convention = new SwaggerResolveConvention();
        convention.apply(project);

        var resolveTask = (ResolveTask) project.getTasks().getByName("resolve");

        assertThat(resolveTask.getOutputDir().getPath()).endsWith("/resources/openapi/yaml/test-api");
        assertThat(resolveTask.getOutputFileName()).isEqualTo(PROJECT_NAME);
        assertThat(resolveTask.getOutputFormat()).isEqualTo(ResolveTask.Format.YAML);
    }

    @Test
    void apply_whenOutputDirSet_shouldAppend() {
        var swagger = ConventionFunctions.requireExtension(project, BuildExtension.class).getSwagger();
        swagger.getApiGroup().set("test-api");
        swagger.getOutputDirectory().set(new File("some/funny/path"));
        var convention = new SwaggerResolveConvention();
        convention.apply(project);

        var resolveTask = (ResolveTask) project.getTasks().getByName("resolve");

        assertThat(resolveTask.getOutputDir().getPath()).endsWith("/some/funny/path/test-api");
        assertThat(resolveTask.getOutputFileName()).isEqualTo(PROJECT_NAME);
        assertThat(resolveTask.getOutputFormat()).isEqualTo(ResolveTask.Format.YAML);
    }
}
