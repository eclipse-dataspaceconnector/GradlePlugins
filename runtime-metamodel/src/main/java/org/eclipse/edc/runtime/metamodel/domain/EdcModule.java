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

package org.eclipse.edc.runtime.metamodel.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * An EDC module.
 */
@JsonDeserialize(builder = EdcModule.Builder.class)
public class EdcModule {
    private final List<String> categories = new ArrayList<>();
    private final Set<EdcServiceExtension> extensions;
    private final List<Service> extensionPoints = new ArrayList<>();
    private String modulePath;
    private String version;
    private String name;


    private EdcModule() {
        extensions = new HashSet<>();
    }

    public Set<EdcServiceExtension> getExtensions() {
        return extensions;
    }

    /**
     * Returns the module id, which corresponds to Maven-style <code>group:artifact</code> coordinates.
     */
    public String getModulePath() {
        return modulePath;
    }

    /**
     * Returns the module version.
     */
    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns categories assigned to the module, or an empty collection.
     */
    public List<String> getCategories() {
        return getAllCategories();
    }

    /**
     * Returns categories assigned to the module itself, plus the categories of all extensions found in the module.
     */
    @JsonIgnore
    public List<String> getAllCategories() {
        var extensionCategories = extensions.stream().flatMap(e -> e.getCategories().stream()).collect(Collectors.toList());
        extensionCategories.addAll(categories);
        return extensionCategories;
    }

    /**
     * Returns services extension points defined in this SPI module, or an empty collection.
     */
    public List<Service> getExtensionPoints() {
        return extensionPoints;
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private final EdcModule module;

        private Builder() {
            module = new EdcModule();
        }

        public static EdcModule.Builder newInstance() {
            return new Builder();
        }

        public Builder extension(EdcServiceExtension extension) {
            module.extensions.add(extension);
            return this;
        }

        public Builder extensions(Set<EdcServiceExtension> extensions) {
            module.extensions.addAll(extensions);
            return this;
        }

        public Builder modulePath(String modulePath) {
            module.modulePath = modulePath;
            return this;
        }

        public Builder version(String version) {
            module.version = version;
            return this;
        }

        public Builder extensionPoints(List<Service> provides) {
            module.extensionPoints.addAll(provides);
            return this;
        }

        public Builder categories(List<String> categories) {
            module.categories.addAll(categories);
            return this;
        }

        public Builder name(String moduleName) {
            module.name = moduleName;
            return this;
        }

        public EdcModule build() {
            requireNonNull(module.modulePath, "id");
            requireNonNull(module.version, "version");
            return module;
        }
    }
}
