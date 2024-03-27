/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.autodoc.core.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.NotAnExtension;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.OptionalService;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.RequiredService;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.SampleExtensionWithoutAnnotation;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.SecondExtension;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.SettingContextExtension;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.SomeOtherService;
import org.eclipse.edc.plugins.autodoc.core.processor.testextensions.SomeService;
import org.eclipse.edc.plugins.autodoc.core.processor.testspi.ExtensionService;
import org.eclipse.edc.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.edc.runtime.metamodel.domain.EdcModule;
import org.eclipse.edc.runtime.metamodel.domain.EdcServiceExtension;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.eclipse.edc.runtime.metamodel.domain.ServiceReference;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_CLASS_PREFIX_SETTING_KEY;
import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_FIELD_PREFIX_SETTING_KEY;
import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_SETTING_DEFAULT_VALUE;
import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_SETTING_ID_KEY;
import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_SPI_MODULE;

public class EdcModuleProcessorTest {

    private static final String EDC_ID = "test-edc-id";
    private static final String EDC_VERSION = "test-edc-version";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @TempDir private Path tempDir;
    private final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

    @Nested
    class CompilerArgs {
        @ParameterizedTest
        @ArgumentsSource(ValidCompilerArgsProvider.class)
        void verifyOptionalCompilerArgs(String edcId, String edcVersion, String edcOutputDir) throws IOException {
            var task = createTask(edcId, edcVersion, edcOutputDir, getFiles("testextensions"));

            var errorMsg = diagnostics.getDiagnostics().stream().map(Object::toString).collect(Collectors.joining(", "));

            assertThat(task.call()).withFailMessage(errorMsg).isTrue();
        }

        @ParameterizedTest
        @ArgumentsSource(InvalidCompilerArgsProvider.class)
        void verifyRequiredCompilerArgs(String edcId, String edcVersion, String edcOutputDir) throws IOException {
            var task = createTask(edcId, edcVersion, edcOutputDir, getFiles("testextensions"));

            assertThat(task.call()).isFalse();
        }

        @Test
        void shouldCreateManifestFile_tempDir() throws IOException {
            var task = createTask("testextensions");

            var result = task.call();

            assertThat(result).isTrue();
            assertThat(Files.list(tempDir))
                    .withFailMessage("Should contain edc.json")
                    .anyMatch(p -> p.getFileName().endsWith("edc.json"));
        }

        @Test
        void shouldOverrideOutputFolder() throws IOException {
            var newTempDir = Files.createTempDirectory("test");
            var task = createTask("-Aedc.version=1.2.3", "-Aedc.id=someid", "-Aedc.outputDir=" + newTempDir, getFiles("testextensions"));

            var result = task.call();

            assertThat(result).isTrue();
            assertThat(Files.list(newTempDir)).anyMatch(p -> p.getFileName().endsWith("edc.json"));
        }

        private static class ValidCompilerArgsProvider implements ArgumentsProvider {
            @Override
            public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
                return Stream.of(
                        Arguments.of("-Aedc.version=1.2.3", "-Aedc.id=someid", "-Aedc.outputDir=build/some/dir"),
                        Arguments.of("-Aedc.version=1.2.3", "-Aedc.id=someid", null),
                        Arguments.of("-Aedc.version=1.2.3", "-Aedc.id=someid", "-Aedc.outputDir=")
                );
            }
        }

        private static class InvalidCompilerArgsProvider implements ArgumentsProvider {
            @Override
            public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
                return Stream.of(
                        Arguments.of("-Aedc.version=1.2.3", "-Aedc.id=", "-Aedc.outputDir=some/dir"),
                        Arguments.of("-Aedc.version=1.2.3", null, "-Aedc.outputDir=some/dir"),
                        Arguments.of("-Aedc.version=", "-Aedc.id=someid", "-Aedc.outputDir=some/dir"),
                        Arguments.of(null, "-Aedc.id=someid", "-Aedc.outputDir=some/dir")
                );
            }

        }
    }

    @Nested
    class Spi {
        @Test
        void verifyCorrectManifest() {
            var task = createTask("testspi");

            task.call();

            var manifest = readManifest();

            assertThat(manifest).hasSize(1);

            var module = manifest.get(0);
            assertThat(module.getName()).isEqualTo(TEST_SPI_MODULE);
            assertThat(module.getVersion()).isEqualTo(EDC_VERSION);
            assertThat(module.getModulePath()).isEqualTo(EDC_ID);
            assertThat(module.getCategories()).hasSize(1).containsOnly("category").isEqualTo(module.getAllCategories());
            assertThat(module.getExtensionPoints()).hasSize(1).containsOnly(new Service(ExtensionService.class.getName()));
            assertThat(module.getExtensions()).isEmpty();
        }
    }

    @Nested
    class Extension {
        @Test
        void verifyManifestContainsExtension() {
            var task = createTask("testextensions");

            task.call();

            var modules = readManifest();

            assertThat(modules).hasSize(1);
            assertThat(modules.get(0).getExtensions())
                    .hasSize(3)
                    .extracting(EdcServiceExtension::getName)
                    .containsOnly(SampleExtensionWithoutAnnotation.class.getSimpleName(),
                            SecondExtension.class.getSimpleName(), SettingContextExtension.class.getSimpleName())
                    .doesNotContain(NotAnExtension.class.getName()); //explicitly exclude this
        }

        @Test
        void verifyManifestContainsCorrectElements() {
            var task = createTask("testextensions");

            task.call();

            var modules = readManifest();
            assertThat(modules).hasSize(1);
            var extensions = modules.get(0).getExtensions();

            assertThat(extensions)
                    .allSatisfy(e -> assertThat(e.getCategories()).isNotNull().isEmpty())
                    .allSatisfy(e -> assertThat(e.getOverview()).isNull())
                    .extracting(EdcServiceExtension::getName)
                    .containsOnly(SampleExtensionWithoutAnnotation.class.getSimpleName(), SecondExtension.class.getSimpleName(), SettingContextExtension.class.getSimpleName());

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

            assertThat(ext1.getConfiguration()).first().isNotNull().satisfies(configuration -> {
                assertThat(configuration).isNotNull();
                assertThat(configuration.getKey()).isEqualTo(SampleExtensionWithoutAnnotation.TEST_SETTING);
                assertThat(configuration.isRequired()).isTrue();
                assertThat(configuration.getDefaultValue()).isEqualTo(TEST_SETTING_DEFAULT_VALUE);
                assertThat(configuration.getDescription()).isNotEmpty();
            });

            var ext2 = extensions.stream().filter(e -> e.getName().equals(SecondExtension.class.getSimpleName()))
                    .findFirst()
                    .orElseThrow();

            assertThat(ext2.getProvides()).isEmpty();

            assertThat(ext2.getReferences())
                    .hasSize(1)
                    .containsOnly(new ServiceReference(SomeService.class.getName(), true));

            assertThat(ext2.getConfiguration()).isEmpty();
        }

        @Test
        void verifyManifestContainsCorrectSettingsWithContext() {
            var task = createTask("testextensions");

            task.call();

            var modules = readManifest();
            assertThat(modules).hasSize(1);
            var extensions = modules.get(0).getExtensions();

            var ext1 = extensions.stream().filter(e -> e.getName().equals(SettingContextExtension.class.getSimpleName()))
                    .findFirst()
                    .orElseThrow();

            assertThat(ext1.getConfiguration())
                    .extracting(ConfigurationSetting::getKey)
                    .contains(TEST_CLASS_PREFIX_SETTING_KEY + TEST_SETTING_ID_KEY, TEST_FIELD_PREFIX_SETTING_KEY + TEST_SETTING_ID_KEY);
        }

    }

    @Test
    void shouldFail_whenSettingIsDefinedInClassNotExtension() {
        var task = createTask("test/NotExtensionWithSetting.java");

        assertThat(task.call()).isFalse();
        assertThat(diagnostics.getDiagnostics()).anySatisfy(diagnostic -> {
            assertThat(diagnostic.getKind()).isEqualTo(Diagnostic.Kind.ERROR);
            assertThat(diagnostic.getMessage(Locale.getDefault())).contains("@Setting");
        });
    }

    @Test
    void shouldPass_whenSettingIsDefinedInExtensionSubclass() {
        var task = createTask("test/ConfigurationExtensionWithSetting.java");

        assertThat(task.call()).isTrue();
    }

    private JavaCompiler.@NotNull CompilationTask createTask(String classPath) {
        try {
            var classes = getFiles(classPath);
            return createTask("-Aedc.id=" + EDC_ID, "-Aedc.version=" + EDC_VERSION, null, classes);
        } catch (IOException e) {
            fail("Cannot create task", e);
            throw new AssertionError(e);
        }
    }

    private JavaCompiler.CompilationTask createTask(String edcId, String edcVersion, String edcOutputDir, File[] classes) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();

        var fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, List.of(tempDir.toFile()));
        fileManager.setLocation(StandardLocation.SOURCE_OUTPUT, List.of(tempDir.toFile()));

        var compilationUnits = fileManager.getJavaFileObjects(classes);

        var options = Stream.of(edcId, edcVersion, edcOutputDir).filter(Objects::nonNull).toList();
        var task = compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits);
        task.setProcessors(Collections.singletonList(new EdcModuleProcessor()));
        return task;
    }

    @NotNull
    private File[] getFiles(String classPath) throws IOException {
        var fullPath = "src/test/java/org/eclipse/edc/plugins/autodoc/core/processor/" + classPath;
        var file = new File(fullPath);
        File[] classes;
        if (file.isDirectory()) {
            classes = Files.list(file.toPath()).map(Path::toFile).toArray(File[]::new);
        } else {
            classes = new File[] { file };
        }
        return classes;
    }

    private List<EdcModule> readManifest() {
        try (var files = Files.list(tempDir)) {
            var url = files.filter(p -> p.endsWith("edc.json")).findFirst().orElseThrow(AssertionError::new).toUri().toURL();
            return OBJECT_MAPPER.readValue(url, new TypeReference<>() { });
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
