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

package org.eclipse.edc.plugins.autodoc.core.processor;

import org.eclipse.edc.plugins.autodoc.core.processor.testspi.ExtensionService;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_SPI_MODULE;
import static org.eclipse.edc.plugins.autodoc.core.processor.TestFunctions.filterManifest;
import static org.eclipse.edc.plugins.autodoc.core.processor.TestFunctions.readManifest;

public class EdcModuleProcessorSpiTest extends EdcModuleProcessorTest {
    @Test
    void verifyCorrectManifest() {
        task.call();

        var manifest = readManifest(filterManifest(tempDir));

        assertThat(manifest).hasSize(1);

        var module = manifest.get(0);
        assertThat(module.getName()).isEqualTo(TEST_SPI_MODULE);
        assertThat(module.getVersion()).isEqualTo(EDC_VERSION);
        assertThat(module.getModulePath()).isEqualTo(EDC_ID);
        assertThat(module.getCategories()).hasSize(1).containsOnly("category").isEqualTo(module.getAllCategories());
        assertThat(module.getExtensionPoints()).hasSize(1).containsOnly(new Service(ExtensionService.class.getName()));
        assertThat(module.getExtensions()).isEmpty();
    }

    @Override
    protected List<File> getCompilationUnits() {
        var f = new File("src/test/java/org/eclipse/edc/plugins/autodoc/core/processor/testspi/");
        try (var files = Files.list(f.toPath())) {
            return files.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
