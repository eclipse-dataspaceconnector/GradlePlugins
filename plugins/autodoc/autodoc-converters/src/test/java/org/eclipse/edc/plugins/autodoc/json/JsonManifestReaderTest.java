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

package org.eclipse.edc.plugins.autodoc.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.plugins.autodoc.spi.ManifestConverterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonManifestReaderTest {

    private final JsonManifestReader reader = new JsonManifestReader(new ObjectMapper());

    @BeforeEach
    void setUp() {

    }

    @Test
    void read() {
        var list = reader.read(readResource("example_manifest.json"));
        assertThat(list)
                .isNotNull()
                .hasSize(96);
    }

    @Test
    void read_inputNotJson() {
        assertThatThrownBy(() -> reader.read(readResource("invalid_manifest.json")))
                .isInstanceOf(ManifestConverterException.class)
                .hasRootCauseInstanceOf(JsonProcessingException.class);
    }

    private InputStream readResource(String filename) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    }
}