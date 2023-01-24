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

package org.eclipse.edc.plugins.edcbuild.tasks;

import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;


public class PrintApiGroupTask extends DefaultTask {

    @TaskAction
    public void printApiGroup() {
        var buildExt = requireExtension(getProject(), BuildExtension.class);
        getProject().getLogger().lifecycle("API Group: {}", buildExt.getSwagger().getApiGroup().getOrElse(""));
    }
}
