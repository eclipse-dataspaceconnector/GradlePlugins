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
        var extension = requireExtension(target, CheckstyleExtension.class);

        guavaWorkaround(target);

        extension.setToolVersion(Versions.CHECKSTYLE);
        extension.setMaxErrors(0);
        extension.setMaxWarnings(0);

        target.getTasks().withType(Checkstyle.class, checkstyle -> checkstyle.reports(r -> {
            r.getHtml().getRequired().set(false);
            r.getXml().getRequired().set(true);
        }));
    }

    /**
     * Ref. <a href="https://github.com/gradle/gradle/issues/27035#issuecomment-1814589243">https://github.com/gradle/gradle/issues/27035#issuecomment-1814589243</a>
     *
     * @param target the project.
     */
    private static void guavaWorkaround(Project target) {
        target.getConfigurations().getByName("checkstyle").getResolutionStrategy().getCapabilitiesResolution()
                .withCapability("com.google.collections:google-collections", details -> details.select("com.google.guava:guava:0"));
    }
}
