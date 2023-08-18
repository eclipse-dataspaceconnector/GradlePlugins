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

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.gradle.api.Project;
import org.gradle.api.tasks.diagnostics.DependencyReportTask;

/**
 * Registers the 'allDependencies' task, used to get all the dependencies from all the submodules.
 */
public class AllDependenciesConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        target.getTasks().register("allDependencies", DependencyReportTask.class);
    }

}
