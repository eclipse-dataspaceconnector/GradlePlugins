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

import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.NotAnExtension;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.OptionalService;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.RequiredService;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.SampleExtensionWithoutAnnotation;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.SecondExtension;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.SomeOtherService;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.SomeService;
import org.eclipse.edc.runtime.metamodel.domain.EdcServiceExtension;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.eclipse.edc.runtime.metamodel.domain.ServiceReference;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.plugins.autodoc.core.processor.TestFunctions.filterManifest;
import static org.eclipse.edc.plugins.autodoc.core.processor.TestFunctions.readManifest;

class EdcModuleProcessorExtensionTest extends EdcModuleProcessorTest {
    @Test
    void verifyManifestContainsExtension() {
        task.call();

        var modules = readManifest(filterManifest(tempDir));
        assertThat(modules).hasSize(1);
        assertThat(modules.get(0).getExtensions())
                .hasSize(2)
                .extracting(EdcServiceExtension::getName)
                .containsOnly(SampleExtensionWithoutAnnotation.class.getSimpleName(),
                        SecondExtension.class.getSimpleName())
                .doesNotContain(NotAnExtension.class.getName()); //explicitly exclude this
    }

    @Test
    void verifyManifestContainsCorrectElements() {
        task.call();

        var modules = readManifest(filterManifest(tempDir));
        assertThat(modules).hasSize(1);
        var extensions = modules.get(0).getExtensions();

        assertThat(extensions)
                .allSatisfy(e -> assertThat(e.getCategories()).isNotNull().isEmpty())
                .allSatisfy(e -> assertThat(e.getOverview()).isNull())
                .extracting(EdcServiceExtension::getName)
                .containsOnly(SampleExtensionWithoutAnnotation.class.getSimpleName(), SecondExtension.class.getSimpleName());

        var ext1 = extensions.stream().filter(e -> e.getName().equals(SampleExtensionWithoutAnnotation.class.getSimpleName()))
                .findFirst()
                .orElseThrow();

        var providedServices = ext1.getProvides();
        assertThat(providedServices).hasSize(2)
                .extracting(Service::getService)
                .containsExactlyInAnyOrder(SomeService.class.getName(), SomeOtherService.class.getName());

        var references = ext1.getReferences();
        assertThat(references.size()).isEqualTo(2);
        assertThat(references).contains(new ServiceReference(OptionalService.class.getName(), false));
        assertThat(references).contains(new ServiceReference(RequiredService.class.getName(), true));

        var configuration = ext1.getConfiguration().get(0);
        assertThat(configuration).isNotNull();
        assertThat(configuration.getKey()).isEqualTo(SampleExtensionWithoutAnnotation.TEST_SETTING);
        assertThat(configuration.isRequired()).isTrue();
        assertThat(configuration.getDescription()).isNotEmpty();

        var ext2 = extensions.stream().filter(e -> e.getName().equals(SecondExtension.class.getSimpleName()))
                .findFirst()
                .orElseThrow();

        assertThat(ext2.getProvides()).isEmpty();

        assertThat(ext2.getReferences())
                .hasSize(1)
                .containsOnly(new ServiceReference(SomeService.class.getName(), true));

        assertThat(ext2.getConfiguration()).isEmpty();
    }

    @Override
    protected List<File> getCompilationUnits() {
        var f = new File("src/test/java/org/eclipse/edc/plugins/autodoc/core/processor/testextensions/");
        try (var files = Files.list(f.toPath())) {
            return files.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
