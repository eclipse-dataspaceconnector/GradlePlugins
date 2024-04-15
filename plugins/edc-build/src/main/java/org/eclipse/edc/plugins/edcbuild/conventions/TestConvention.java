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
import org.jetbrains.annotations.NotNull;

import static java.util.Optional.ofNullable;

/**
 * Configures the use of JUnit, the tagging mechanism and also configures the test logging
 */
class TestConvention implements EdcConvention {
    private static void determineJunitPlatform(Test testTask) {
        // parse task exclusion
        var excludedTagsProperty = System.getProperty("excludeTags");
        var excludedTags = getTags(excludedTagsProperty);


        // Target all type of test e.g. -DrunAllTests="true"
        var runAllTests = Boolean.parseBoolean(System.getProperty("runAllTests", "false"));
        if (runAllTests) {
            // honor excluded tags -> blacklisting
            if (excludedTags.length > 0) {
                testTask.useJUnitPlatform(platform -> platform.excludeTags(excludedTags));
            } else {
                testTask.useJUnitPlatform();
            }
        } else {
            var includeTagsProperty = System.getProperty("includeTags");
            var includedTags = getTags(includeTagsProperty);

            // white-list included tags...
            if (includedTags.length > 0) {
                testTask.useJUnitPlatform(platform -> platform.includeTags(includedTags));
                //... and possibly black-list excluded tags
                if (excludedTags.length > 0) {
                    testTask.useJUnitPlatform(platform -> platform.excludeTags(excludedTags));
                }
            } else {
                // no point in evaluating other excluded tags, if only unit tests are run
                testTask.useJUnitPlatform(platform -> platform.excludeTags("IntegrationTest"));
            }
        }
    }

    @NotNull
    private static String[] getTags(String tagsSeparatedByComma) {
        return ofNullable(tagsSeparatedByComma)
                .map(prop -> prop.split(","))
                .orElse(new String[0]);
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
