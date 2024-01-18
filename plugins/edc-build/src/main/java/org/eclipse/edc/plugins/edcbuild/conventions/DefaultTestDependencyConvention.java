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

import static java.lang.String.format;
import static org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME;
import static org.gradle.api.plugins.JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME;

/**
 * Applies default test dependencies to all "java-library" projects: JUnit, Mockito and AssertJ in their respective
 * configurations.
 */
class DefaultTestDependencyConvention implements EdcConvention {

    @Override
    public void apply(Project target) {
        target.getPluginManager().withPlugin("java-library", plugin -> {

            var d = target.getDependencies();

            //test classpath dependencies
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-api:%s", Versions.JUPITER));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-params:%s", Versions.JUPITER));
            d.add(TEST_RUNTIME_ONLY_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-engine:%s", Versions.JUPITER));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.mockito:mockito-core:%s", Versions.MOCKITO));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.assertj:assertj-core:%s", Versions.ASSERTJ));
        });
    }

}
