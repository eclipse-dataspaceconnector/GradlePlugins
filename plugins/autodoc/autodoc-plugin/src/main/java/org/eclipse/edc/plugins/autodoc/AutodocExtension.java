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

package org.eclipse.edc.plugins.autodoc;

import org.gradle.api.provider.Property;

import java.io.File;

public abstract class AutodocExtension {
    private boolean includeTransitive = true;

    /**
     * Overrides the default output directory relative to the current project dir
     */
    public abstract Property<File> getOutputDirectory();

    /**
     * Override the version of the annotation processor module to use. The default is to take the same version as the plugin.
     */
    public abstract Property<String> getProcessorVersion();

    /**
     * Optional input to specify, where additional autodoc manifests that are to be merged, are located on the filesystem.
     * Use this, if you have a directory that contains multiple autodoc manifests, e.g. of third-party or transitive deps.
     * <p>
     * If this is set, the merge task will take all manifests found in this directory and append them to the {@code manifest.json} file.
     * Usually, this points to wherever the downloaded manifests are store.
     *
     * @see AutodocExtension#getDownloadDirectory()
     */
    public abstract Property<File> getAdditionalInputDirectory();

    /**
     * Retrieves the directory where downloaded manifests are to be stored. Defaults to {@code <rootProject>/build/manifests}
     *
     * @return The property representing the download directory, or null if not specified.
     */
    public abstract Property<File> getDownloadDirectory();


    /**
     * Determines whether to include transitive dependencies in the merge process.
     * If set to {@code true}, the merge task will download the manifests of transitive (EDC) dependencies and include them in the merged manifest.
     * If set to {@code false}, only the direct dependencies will be merged.
     *
     * @return {@code true} if transitive dependencies should be included, {@code false} otherwise.
     */
    public boolean isIncludeTransitive() {
        return includeTransitive;
    }

    public void setIncludeTransitive(boolean includeTransitive) {
        this.includeTransitive = includeTransitive;
    }
}
