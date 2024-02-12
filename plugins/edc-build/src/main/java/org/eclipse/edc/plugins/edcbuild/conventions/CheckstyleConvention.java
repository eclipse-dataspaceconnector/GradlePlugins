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

import org.eclipse.edc.plugins.edcbuild.Versions;
import org.gradle.api.Project;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Applies default configuration for the checkstyle plugin:
 * <ul>
 *     <li>applies tool version 10.0</li>
 *     <li>sets maxError to 0</li>
 *     <li>requires xml reports</li>
 * </ul>
 */
class CheckstyleConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        var cse = requireExtension(target, CheckstyleExtension.class);

        cse.setToolVersion(Versions.CHECKSTYLE);
        cse.setMaxErrors(0);
        target.getTasks().withType(Checkstyle.class, cs -> cs.reports(r -> {
            r.getHtml().getRequired().set(false);
            r.getXml().getRequired().set(true);
        }));
    }
}
