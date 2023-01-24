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

import org.eclipse.edc.plugins.openapimerger.OpenApiMergerPlugin;
import org.eclipse.edc.plugins.openapimerger.tasks.MergeApiSpecByPathTask;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiMergerPluginTest {

    private Project project;

    @BeforeEach
    void setUp() {
        project = ProjectBuilder.builder().build();
        project.getPlugins().apply(OpenApiMergerPlugin.class);
    }

    @Test
    void verify_hasMergerTask() {
        assertThat(project.getTasks().findByName(MergeApiSpecByPathTask.NAME)).isNotNull();
    }


    @Test
    void verify_pluginIsOnlyAppliedToRootProject() {
        var subproj = ProjectBuilder.builder().withParent(project).build();

        subproj.getPlugins().apply(OpenApiMergerPlugin.class);

        assertThat(subproj.getTasks().findByName(MergeApiSpecByPathTask.NAME)).isNull();
    }

}