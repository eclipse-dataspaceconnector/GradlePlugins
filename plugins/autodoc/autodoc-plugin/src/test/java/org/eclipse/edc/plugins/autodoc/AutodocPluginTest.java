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

import org.eclipse.edc.plugins.autodoc.tasks.MergeManifestsTask;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.plugins.autodoc.AutodocPlugin.AUTODOC_TASK_NAME;


public class AutodocPluginTest {
    @Test
    public void pluginRegistersAutodocTask() {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply(AutodocPlugin.class);

        // Verify the result
        var tasks = project.getTasks();
        assertThat(tasks.findByName(AUTODOC_TASK_NAME)).isNotNull();
        assertThat(tasks.findByName(MergeManifestsTask.NAME)).isNotNull();
    }
}
