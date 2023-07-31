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

package org.eclipse.edc.plugins.autodoc.spi;

import org.eclipse.edc.runtime.metamodel.domain.EdcModule;

import java.io.InputStream;
import java.util.List;

/**
 * Reads a manifest file, which contains a {@link List} of {@link EdcModule}. Implementations are file-format specific.
 */
public interface ManifestReader {
    /**
     * Reads an input stream, e.g. a FileInput stream, and interprets it
     *
     * @param inputStream The input
     */
    List<EdcModule> read(InputStream inputStream);
}
