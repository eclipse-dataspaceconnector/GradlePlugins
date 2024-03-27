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

import net.steppschuh.markdowngenerator.Markdown;
import net.steppschuh.markdowngenerator.MarkdownElement;
import net.steppschuh.markdowngenerator.table.Table;
import org.eclipse.edc.plugins.autodoc.spi.ManifestConverterException;
import org.eclipse.edc.plugins.autodoc.spi.ManifestRenderer;
import org.eclipse.edc.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.edc.runtime.metamodel.domain.ModuleType;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.eclipse.edc.runtime.metamodel.domain.ServiceReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static net.steppschuh.markdowngenerator.Markdown.bold;
import static net.steppschuh.markdowngenerator.Markdown.code;
import static net.steppschuh.markdowngenerator.Markdown.heading;
import static net.steppschuh.markdowngenerator.Markdown.italic;
import static net.steppschuh.markdowngenerator.Markdown.unorderedList;

public class MarkdownManifestRenderer implements ManifestRenderer {

    private static final String NEWLINE = System.lineSeparator();
    private final OutputStream output;
    private final StringBuilder stringBuilder;

    public MarkdownManifestRenderer(OutputStream output) {
        this.output = output;
        stringBuilder = new StringBuilder();
    }

    @Override
    public void renderDocumentHeader() {
        stringBuilder.append(heading(DOCUMENT_HEADING, 1)).append(NEWLINE);
        stringBuilder.append(NEWLINE);
    }

    @Override
    public void renderModuleHeading(@Nullable String moduleName, @NotNull String modulePath, @NotNull String version) {
        var name = ofNullable(moduleName).orElse(modulePath);

        var moduleHeading = heading(format("Module `%s:%s`", name, version), 2);
        stringBuilder.append(moduleHeading).append(NEWLINE);

        if (moduleName != null) {
            stringBuilder.append(italic(modulePath)).append(NEWLINE);
        }
        stringBuilder.append(NEWLINE);
    }

    @Override
    public void renderCategories(List<String> categories) {
        // append categories as italic text
        var cat = categories
                .stream()
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.joining(","));

        if (cat.isEmpty()) {
            cat = NONE;
        }

        stringBuilder.append(italic(format("Categories: %s", cat))).append(NEWLINE);
        stringBuilder.append(NEWLINE);
    }

    @Override
    public void renderExtensionPoints(List<Service> extensionPoints) {
        // append extension points
        stringBuilder.append(heading(EXTENSION_POINTS, 3)).append(NEWLINE);
        stringBuilder.append(listOrNone(unorderedList(extensionPoints.stream().map(s -> code(s.getService())).toList().toArray()))).append(NEWLINE);
        stringBuilder.append(NEWLINE);
    }

    @Override
    public void renderExtensionHeading() {
        stringBuilder.append(heading(EXTENSIONS, 3)).append(NEWLINE);
    }

    @Override
    public void renderExtensionHeader(@NotNull String className, String name, String overview, ModuleType type) {
        stringBuilder.append(heading("Class: " + code(className), 4)).append(NEWLINE);

        stringBuilder.append(bold("Name:")).append(format(" \"%s\"", name)).append(NEWLINE);
        if (overview != null) {
            stringBuilder.append(NEWLINE).append(bold("Overview:")).append(" ").append(overview).append(NEWLINE).append(NEWLINE);
        }
        stringBuilder.append(NEWLINE);
    }

    @Override
    public void renderConfigurations(List<ConfigurationSetting> configuration) {
        // add configuration table
        var tableBuilder = new Table.Builder()
                .addRow("Key", "Required", "Type", "Default", "Pattern", "Min", "Max", "Description");



        configuration.forEach(setting -> tableBuilder.addRow(
                code(setting.getKey()),
                setting.isRequired() ? code("*") : null,
                code(setting.getType()),
                ofNullable(setting.getDefaultValue()).map(Markdown::code).orElse(null),
                ofNullable(setting.getPattern()).map(Markdown::code).orElse(null),
                ofNullable(setting.getMinimum()).map(m -> code(String.valueOf(m))).orElse(null),
                ofNullable(setting.getMaximum()).map(m -> code(String.valueOf(m))).orElse(null),
                setting.getDescription()));

        stringBuilder.append(heading("Configuration: ", 5));
        if (!configuration.isEmpty()) {
            stringBuilder.append(NEWLINE).append(NEWLINE).append(tableBuilder.build()).append(NEWLINE);
        } else {
            stringBuilder.append(italic(NONE)).append(NEWLINE);
        }
        stringBuilder.append(NEWLINE);
    }

    @Override
    public void renderProvidedServices(List<Service> provides) {
        // add exposed services
        stringBuilder.append(heading("Provided services:", 5)).append(NEWLINE);
        stringBuilder.append(listOrNone(provides.stream().map(s -> code(s.getService())).toList().toArray())).append(NEWLINE);
        stringBuilder.append(NEWLINE);
    }

    @Override
    public void renderReferencedServices(List<ServiceReference> references) {
        // add injected services
        stringBuilder.append(heading("Referenced (injected) services:", 5)).append(NEWLINE);
        stringBuilder.append(listOrNone(references.stream().map(s -> format("%s (%s)", code(s.getService()), s.isRequired() ? "required" : "optional")).toList().toArray())).append(NEWLINE);
        stringBuilder.append(NEWLINE);
    }

    @Override
    public OutputStream finalizeRendering() {
        try {
            output.write(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ManifestConverterException(e);
        }
        return output;
    }

    private MarkdownElement listOrNone(Object... items) {
        if (items.length == 0 || Arrays.stream(items).allMatch(o -> o.toString().isEmpty())) {
            return italic(NONE);
        } else {
            return unorderedList(items);
        }
    }
}
