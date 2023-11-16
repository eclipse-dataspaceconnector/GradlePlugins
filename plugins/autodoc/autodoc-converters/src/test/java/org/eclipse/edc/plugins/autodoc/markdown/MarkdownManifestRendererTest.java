/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.plugins.autodoc.markdown;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.plugins.autodoc.spi.ManifestWriter;
import org.eclipse.edc.runtime.metamodel.domain.EdcModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkdownManifestRendererTest {

    private ManifestWriter writer;
    private final ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        writer = new ManifestWriter(new MarkdownManifestRenderer(testOutputStream));
    }

    @Test
    void convert_exampleJson() {
        var list = generateManifest("example_manifest.json");

        var result = writer.convert(list);

        assertThat(result).isNotNull().isEqualTo(testOutputStream);
    }

    @Test
    void convert_simpleJson() {
        var list = generateManifest("simple_manifest.json");

        var result = writer.convert(list);

        assertThat(result).isNotNull().isEqualTo(testOutputStream);
    }

    @Test
    void convert_emptyObject() {
        var list = List.of(EdcModule.Builder.newInstance().modulePath("foo").version("0.1.0-bar").build());
        var result = writer.convert(list);

        assertThat(result).isNotNull().isEqualTo(testOutputStream).extracting(Object::toString).satisfies(markdown -> {
            assertThat(markdown).contains("Module `foo:0.1.0-bar`");
            assertThat(markdown).contains("### Extension points");
            assertThat(markdown).contains("### Extensions");
            assertThat(markdown).doesNotContain("Configuration:");
        });
    }

    private List<EdcModule> generateManifest(String filename) {
        try (var stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            return new ObjectMapper().readValue(stream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
