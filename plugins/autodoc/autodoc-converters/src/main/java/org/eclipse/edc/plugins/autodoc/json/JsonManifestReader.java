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

package org.eclipse.edc.plugins.autodoc.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.plugins.autodoc.spi.ManifestConverterException;
import org.eclipse.edc.plugins.autodoc.spi.ManifestReader;
import org.eclipse.edc.runtime.metamodel.domain.EdcModule;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class JsonManifestReader implements ManifestReader {
    private static final TypeReference<List<EdcModule>> MODULE_TYPE_REF = new TypeReference<>() {
    };
    private final ObjectMapper objectMapper;

    public JsonManifestReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<EdcModule> read(InputStream inputStream) {
        try {
            return objectMapper.readValue(new InputStreamReader(new BufferedInputStream(inputStream)), MODULE_TYPE_REF);
        } catch (IOException e) {
            throw new ManifestConverterException(e);
        }
    }
}
