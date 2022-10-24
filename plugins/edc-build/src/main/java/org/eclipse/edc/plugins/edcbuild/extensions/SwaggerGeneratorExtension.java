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

package org.eclipse.edc.plugins.edcbuild.extensions;

import org.gradle.api.provider.Property;

import java.io.File;
import java.util.Set;

public abstract class SwaggerGeneratorExtension {

    private Set<String> resourcePackages = Set.of("org.eclipse.edc");
    private String mergedFileName = "openapi";
    private String mergedFileExtension = "yaml";
    private String description = "All files merged by open api merger";

    public abstract Property<String> getOutputFilename();

    public abstract Property<File> getOutputDirectory();

    public Set<String> getResourcePackages() {
        return resourcePackages;
    }

    public void setResourcePackages(Set<String> resourcePackages) {
        this.resourcePackages = resourcePackages;
    }

    /**
     * OpenApi Title of the generated and merged openapi.yaml file
     */
    public abstract Property<String> getTitle();

    /**
     * OpenAPI description of the merged openapi.yaml file
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * File name of the file into which all the openapi files of all the modules are merged.
     * Defaults to "openapi"
     */
    public String getMergedFileName() {
        return mergedFileName;
    }

    public void setMergedFileName(String mergedFileName) {
        this.mergedFileName = mergedFileName;
    }

    /**
     * File extension of the file into which all the openapi files of all the modules are merged.
     * Defaults to "yaml"
     */
    public String getMergedFileExtension() {
        return mergedFileExtension;
    }

    public void setMergedFileExtension(String mergedFileExtension) {
        this.mergedFileExtension = mergedFileExtension;
    }
}
