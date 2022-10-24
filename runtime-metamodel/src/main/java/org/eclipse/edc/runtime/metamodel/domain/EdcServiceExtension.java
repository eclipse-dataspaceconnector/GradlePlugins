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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@JsonDeserialize(builder = EdcServiceExtension.Builder.class)
public class EdcServiceExtension {

    private final List<String> categories = new ArrayList<>();
    private final List<Service> provides = new ArrayList<>();
    private final List<ServiceReference> references = new ArrayList<>();
    private final List<ConfigurationSetting> configuration = new ArrayList<>();

    private String name;
    private ModuleType type = ModuleType.EXTENSION;
    private String overview;
    private String className;

    private EdcServiceExtension() {
    }

    /**
     * Returns the module readable name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the module type.
     */
    public ModuleType getType() {
        return type;
    }

    /**
     * Returns categories assigned to the module, or an empty collection.
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * Returns services provided by this extension module, or an empty collection.
     */
    public List<Service> getProvides() {
        return provides;
    }

    /**
     * Returns services that are provided by other modules and referenced in the current module, or an empty collection.
     */
    public List<ServiceReference> getReferences() {
        return references;
    }

    /**
     * Returns the configuration settings for this module.
     */
    public List<ConfigurationSetting> getConfiguration() {
        return configuration;
    }

    /**
     * Returns a Markdown-formatted description of this module.
     */
    public String getOverview() {
        return overview;
    }

    public String getClassName() {
        return className;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private final EdcServiceExtension serviceExtension;

        private Builder() {
            serviceExtension = new EdcServiceExtension();
        }

        @JsonCreator
        public static Builder newInstance() {
            return new Builder();
        }


        public Builder name(String name) {
            serviceExtension.name = name;
            return this;
        }

        public Builder type(ModuleType type) {
            serviceExtension.type = type;
            return this;
        }

        public Builder categories(List<String> categories) {
            serviceExtension.categories.addAll(categories);
            return this;
        }

        public Builder provides(List<Service> provides) {
            serviceExtension.provides.addAll(provides);
            return this;
        }


        public Builder references(List<ServiceReference> requires) {
            serviceExtension.references.addAll(requires);
            return this;
        }

        public Builder configuration(List<ConfigurationSetting> configuration) {
            serviceExtension.configuration.addAll(configuration);
            return this;
        }

        public Builder overview(String overview) {
            serviceExtension.overview = overview;
            return this;
        }

        public Builder className(String className) {
            serviceExtension.className = className;
            return this;
        }

        public EdcServiceExtension build() {
            requireNonNull(serviceExtension.name, "name");
            requireNonNull(serviceExtension.className, "className");
            return serviceExtension;
        }
    }
}
