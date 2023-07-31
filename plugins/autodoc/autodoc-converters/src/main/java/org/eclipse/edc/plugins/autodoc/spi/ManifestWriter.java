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
import org.eclipse.edc.runtime.metamodel.domain.EdcServiceExtension;

import java.io.OutputStream;
import java.util.List;
import java.util.Set;

/**
 * Reads any input to a manifest, which is represented as {@link List} of {@link EdcModule}.
 * All model objects are delegated down to implementors using callbacks.
 */
public class ManifestWriter {

    private final ManifestRenderer renderer;

    public ManifestWriter(ManifestRenderer renderer) {
        this.renderer = renderer;
    }

    public OutputStream convert(List<EdcModule> input) {
        beginConversion(input);
        return renderer.finalizeRendering();
    }

    protected void beginConversion(List<EdcModule> input) {
        renderer.handleDocumentHeader();
        input.forEach(this::handleModule);
    }


    /**
     * Delegates one top-level element, which are {@link EdcModule} objects.
     * Do not override unless absolutely necessary!
     *
     * @param edcModule The module to render
     */
    protected void handleModule(EdcModule edcModule) {
        renderer.handleModuleHeading(edcModule.getName(), edcModule.getModulePath(), edcModule.getVersion());

        // append categories as italic text
        renderer.handleModuleCategories(edcModule.getCategories());

        // append extension points
        renderer.handleExtensionPoints(edcModule.getExtensionPoints());

        // append extensions
        handleExtensions(edcModule.getExtensions());
    }

    protected void handleExtensions(Set<EdcServiceExtension> extensions) {
        renderer.handleExtensionHeading();
        extensions.forEach(this::handleServiceExtension);
    }

    protected void handleServiceExtension(EdcServiceExtension serviceExtension) {
        renderer.handleExtensionHeader(serviceExtension.getClassName(), serviceExtension.getName(), serviceExtension.getOverview(), serviceExtension.getType());

        // add configuration table
        renderer.handleConfigurations(serviceExtension.getConfiguration());

        // add exposed services
        renderer.handleExposedServices(serviceExtension.getProvides());

        // add injected services
        renderer.handleReferencedServices(serviceExtension.getReferences());
    }

}
