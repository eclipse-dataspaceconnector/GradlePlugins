/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.plugins.edcbuild.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskAction;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;


public class PrintClasspathTask extends DefaultTask {

    @TaskAction
    public void printClasspath() {
        var classpath = requireExtension(getProject(), JavaPluginExtension.class)
                .getSourceSets()
                .getByName("main")
                .getRuntimeClasspath()
                .getAsPath();

        System.out.println(classpath);
    }

}
