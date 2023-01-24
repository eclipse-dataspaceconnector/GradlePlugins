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
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;

import static java.util.Optional.ofNullable;

/**
 * Configures the use of JUnit, the tagging mechanism and also configures the test logging
 */
class TestConvention implements EdcConvention {
    private static void determineJunitPlatform(Test testTask) {
        // Target all type of test e.g. -DrunAllTests="true"
        var runAllTests = Boolean.parseBoolean(System.getProperty("runAllTests", "false"));
        if (runAllTests) {
            testTask.useJUnitPlatform();
        } else {
            var includeTagsProperty = System.getProperty("includeTags");
            var tags = ofNullable(includeTagsProperty)
                    .map(prop -> prop.split(","))
                    .orElse(new String[0]);

            if (tags.length > 0) {
                testTask.useJUnitPlatform(platform -> platform.includeTags(tags));
            } else {
                testTask.useJUnitPlatform(platform -> platform.excludeTags("IntegrationTest"));
            }
        }
    }

    @Override
    public void apply(Project target) {
        target.getTasks().withType(Test.class, testTask -> {
            determineJunitPlatform(testTask);
            configureLogging(target.hasProperty("verboseTest"), testTask);
            testTask.setForkEvery(100L);
        });
    }

    private void configureLogging(boolean verboseTest, Test testTask) {
        testTask.testLogging(logging -> {
            var events = verboseTest ?
                    new Object[]{ "started", "passed", "skipped", "failed", "standard_out", "standard_error" } :
                    new Object[]{ "failed" };
            logging.events(events);

            logging.setShowStackTraces(true);
            logging.setExceptionFormat(TestExceptionFormat.FULL);
        });
    }
}
