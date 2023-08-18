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
    private ByteArrayOutputStream testOutputStream;

    @BeforeEach
    void setUp() {
        testOutputStream = new ByteArrayOutputStream();
        writer = new ManifestWriter(new MarkdownManifestRenderer(testOutputStream));
    }

    @Test
    void convert_exampleJson() {
        var list = generateManifest("example_manifest.json");
        var os = writer.convert(list);

        var result = testOutputStream.toString();
        assertThat(result).isNotNull();
        assertThat(os).isEqualTo(testOutputStream);

    }

    @Test
    void convert_simpleJson() {
        var list = generateManifest("simple_manifest.json");
        var os = writer.convert(list);

        var result = testOutputStream.toString();
        assertThat(result).isNotNull();
        assertThat(os).isEqualTo(testOutputStream);

    }

    @Test
    void convert_emptyObject() {
        var list = List.of(EdcModule.Builder.newInstance().modulePath("foo").version("0.1.0-bar").build());
        var os = writer.convert(list);

        var result = testOutputStream.toString();
        assertThat(result).isNotNull();
        assertThat(os).isEqualTo(testOutputStream);

        assertThat(result).contains("Module `foo:0.1.0-bar`");
        assertThat(result).contains("### Extension points");
        assertThat(result).contains("### Extensions");
        assertThat(result).doesNotContain("Configuration:");
    }

    private List<EdcModule> generateManifest(String filename) {
        try {
            return new ObjectMapper().readValue(Thread.currentThread().getContextClassLoader().getResourceAsStream(filename), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}