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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.runtime.metamodel.domain.EdcModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestFunctions {
    private static final TypeReference<List<EdcModule>> TYPE_REFERENCE = new TypeReference<>() {
    };
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static Path filterManifest(Path tempDir) {
        try (var files = Files.list(tempDir)) {
            return files.filter(p -> p.endsWith("edc.json")).findFirst().orElseThrow(AssertionError::new);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public static List<EdcModule> readManifest(Path manifestFile) {
        try (var stream = manifestFile.toUri().toURL().openStream()) {
            return OBJECT_MAPPER.readValue(stream, TYPE_REFERENCE);
        } catch (IOException e) {
            throw new AssertionError("Unexpected error while reading manifest", e);
        }
    }

}
