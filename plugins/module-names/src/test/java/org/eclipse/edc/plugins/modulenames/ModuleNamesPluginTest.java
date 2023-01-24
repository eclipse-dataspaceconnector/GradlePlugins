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

package org.eclipse.edc.plugins.modulenames;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.ProjectConfigurationException;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchException;

class ModuleNamesPluginTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void verifyFailsOnDuplicatedModule() throws IOException {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();

        project.getPlugins().apply(ModuleNamesPlugin.class);

        var sp1 = createProjectDirectory(project, "subp1");
        var sp2 = createProjectDirectory(project, "subp2");

        // two sub-sub modules have the same name -> violation
        createProjectDirectory(sp1, "subsub");
        createProjectDirectory(sp2, "subsub");

        var ex = catchException(() -> ((DefaultProject) project).evaluate());
        assertThat(ex)
                .isInstanceOf(ProjectConfigurationException.class)
                .hasMessageStartingWith("A problem occurred configuring root project 'test'.")
                .hasRootCauseInstanceOf(GradleException.class);
        assertThat(ex.getCause())
                .hasMessageContaining("Duplicate module names found");

    }

    @Test
    void verifySucceedsWhenUniqueModuleNames() throws IOException {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();

        project.getPlugins().apply(ModuleNamesPlugin.class);

        var sp1 = createProjectDirectory(project, "subp1");
        var sp2 = createProjectDirectory(project, "subp2");

        // two sub-sub modules have the same name -> violation
        createProjectDirectory(sp1, "subsubX");
        createProjectDirectory(sp2, "subsubY");

        assertThatNoException().isThrownBy(() -> ((DefaultProject) project).evaluate());

    }

    private Project createProjectDirectory(Project parent, String name) throws IOException {
        var buildDir = new File(String.format("./build/projects/%s/", UUID.randomUUID()));
        assertThat(buildDir.mkdirs()).isTrue();

        var buildFile = new File(buildDir, "build.gradle");
        assertThat(buildFile.createNewFile()).isTrue();
        return ProjectBuilder.builder().withParent(parent).withName(name).withProjectDir(buildDir).build();
    }

}