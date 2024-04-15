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

package org.eclipse.edc.plugins.edcbuild.conventions;

import org.eclipse.edc.plugins.edcbuild.tasks.PrintClasspathTask;
import org.gradle.api.Project;

public class PrintClasspathConvention implements EdcConvention {
    @Override
    public void apply(Project target) {
        target.getTasks().register("printClasspath", PrintClasspathTask.class)
                .configure(t -> t.dependsOn("compileJava"));
    }
}
