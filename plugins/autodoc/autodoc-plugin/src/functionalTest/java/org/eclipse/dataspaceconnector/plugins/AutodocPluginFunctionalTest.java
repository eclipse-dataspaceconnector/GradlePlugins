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

package org.eclipse.dataspaceconnector.plugins;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;


public class AutodocPluginFunctionalTest {
    @Test
    public void canRunTask() throws IOException {
        // Setup the test build
        File projectDir = new File("build/functionalTest");
        Files.createDirectories(projectDir.toPath());
        writeString(new File(projectDir, "settings.gradle"), "");
        writeString(new File(projectDir, "build.gradle"),
                "plugins {" +
                        "  id(\"autodoc\")" +
                        "}");
        // Run the build
        var result = GradleRunner.create()
                .forwardOutput()
                .withPluginClasspath()
                .withArguments("autodoc")
                .withProjectDir(projectDir)
                .build();

        // Verify the result
        assertThat(result.getOutput()).contains("Hello from plugin 'org.eclipse.dataspaceconnector.autodoc'");
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
