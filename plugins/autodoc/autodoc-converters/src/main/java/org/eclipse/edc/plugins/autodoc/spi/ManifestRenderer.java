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

package org.eclipse.edc.plugins.autodoc.spi;

import org.eclipse.edc.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.edc.runtime.metamodel.domain.EdcServiceExtension;
import org.eclipse.edc.runtime.metamodel.domain.ModuleType;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.eclipse.edc.runtime.metamodel.domain.ServiceReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.List;

/**
 * The ManifestRenderer interface provides callback methods to render a manifest document.
 */
public interface ManifestRenderer {
    String DOCUMENT_HEADING = "EDC Autodoc Manifest";
    String EXTENSION_POINTS = "Extension points";
    String EXTENSIONS = "Extensions";
    String NONE = "None";

    /**
     * Renders the document header
     */
    void renderDocumentHeader();

    /**
     * Render the heading for a module.
     */
    void renderModuleHeading(@Nullable String moduleName, @NotNull String modulePath, @NotNull String version);

    /**
     * Render a document section for the categories of a module.
     *
     * @param categories May be empty, may contain empty strings.
     */
    void renderCategories(List<String> categories);

    /**
     * Handles the creation of an {@link EdcServiceExtension} object, which usually represents all the services in a module, that are intended to be implemented or subclassed.
     */
    void renderExtensionPoints(List<Service> extensionPoints);

    /**
     * Create a sub-heading for an extension
     */
    void renderExtensionHeading();

    /**
     * Render the header for an extension.
     *
     * @param className The fully-qualified java classname.
     * @param name      The human-readable extension name. May be null.
     * @param overview  A string containing more information about the extension. Can be null, empty or even multiline.
     * @param type      The type of extension module, it can either be an SPI module or an implementation module
     */
    void renderExtensionHeader(@NotNull String className, @Nullable String name, @Nullable String overview, ModuleType type);

    /**
     * Render all configuration values that are declared by a particular extension
     */
    void renderConfigurations(List<ConfigurationSetting> configuration);

    /**
     * Render all services, that are <em>provided</em> by a particular module.
     */
    void renderExposedServices(List<Service> provides);

    /**
     * Render all services, that an extension <em>requires</em>, i.e. that must be provided by <em>other extensions</em>.
     */
    void renderReferencedServices(List<ServiceReference> references);

    /**
     * Finalizes the conversion, e.g. by adding closing tags, adding footnotes, validation, etc.
     *
     * @return An {@link OutputStream} that contains the rendered document.
     */
    OutputStream finalizeRendering();
}
