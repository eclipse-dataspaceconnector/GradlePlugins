/*
 *  Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.plugins.modulenames;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Custom grade plugin to avoid module name duplications.
 * Checks between modules with a gradle build file that their names are unique in the whole project.
 * `samples` and `system-tests` modules are excluded.
 * <p>
 * Ref: <a href="https://github.com/gradle/gradle/issues/847">Github Issue</a>
 */
public class ModuleNamesPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.afterEvaluate(new CheckModuleNamesAction());
    }

}
