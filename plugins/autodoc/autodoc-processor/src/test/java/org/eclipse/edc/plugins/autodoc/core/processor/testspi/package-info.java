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

/**
 * This is a sample SPI package.
 */
@Spi(value = TEST_SPI_MODULE, categories = { "category" })
package org.eclipse.edc.plugins.autodoc.core.processor.testspi;

import org.eclipse.edc.runtime.metamodel.annotation.Spi;

import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_SPI_MODULE;
