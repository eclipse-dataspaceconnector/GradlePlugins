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

import org.gradle.api.Project;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;
import org.gradle.testing.jacoco.tasks.JacocoReport;

class JacocoConvention implements EdcConvention {
    @Override
    public void apply(Project target) {
        if ("true".equals(System.getenv("JACOCO"))) {
            target.getPluginManager().apply(JacocoPlugin.class);
        }

        var jacocoReport = target.getTasks().findByName("jacocoTestReport");
        if (jacocoReport instanceof JacocoReport) {
            ((JacocoReport) jacocoReport).getReports().getXml().getRequired().set(true);
        }
    }
}
