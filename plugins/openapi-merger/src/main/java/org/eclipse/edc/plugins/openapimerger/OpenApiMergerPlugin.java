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

package org.eclipse.edc.plugins.openapimerger;

import com.rameshkp.openapi.merger.gradle.plugin.OpenApiMergerGradlePlugin;
import org.eclipse.edc.plugins.openapimerger.tasks.MergeApiSpecByPathTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Custom grade plugin to avoid module name duplications.
 * Checks between modules with a gradle build file that their names are unique in the whole project.
 * `samples` and `system-tests` modules are excluded.
 * <p>
 * Ref: <a href="https://github.com/gradle/gradle/issues/847">Github Issue</a>
 */
public class OpenApiMergerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        if (project == project.getRootProject()) {
            project.getPlugins().apply(OpenApiMergerGradlePlugin.class);
            project.getTasks().register(MergeApiSpecByPathTask.NAME, MergeApiSpecByPathTask.class);
        }
    }

}
