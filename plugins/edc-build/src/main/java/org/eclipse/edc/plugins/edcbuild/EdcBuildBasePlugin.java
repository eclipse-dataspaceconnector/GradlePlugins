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

package org.eclipse.edc.plugins.edcbuild;

import com.autonomousapps.DependencyAnalysisPlugin;
import io.github.gradlenexus.publishplugin.NexusPublishPlugin;
import org.eclipse.edc.plugins.autodoc.AutodocPlugin;
import org.eclipse.edc.plugins.modulenames.ModuleNamesPlugin;
import org.eclipse.edc.plugins.openapimerger.OpenApiMergerPlugin;
import org.eclipse.edc.plugins.testsummary.TestSummaryPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.crypto.checksum.ChecksumPlugin;
import org.gradle.testing.jacoco.plugins.JacocoPlugin;

/**
 * Defines the capabilities of the EDC build as specified in the Gradle Documentation
 *
 * @see <a href="https://docs.gradle.org/current/userguide/designing_gradle_plugins.html">Gradle Documentation</a>
 */
public class EdcBuildBasePlugin implements Plugin<Project> {
    private static void defineCapabilities(Project target) {

        target.getPlugins().apply(JavaLibraryPlugin.class);
        target.getPlugins().apply(JacocoPlugin.class);
        target.getPlugins().apply(AutodocPlugin.class);
        target.getPlugins().apply(CheckstylePlugin.class);
        target.getPlugins().apply(MavenPublishPlugin.class);
        target.getPlugins().apply(JavaPlugin.class);
        target.getPlugins().apply(TestSummaryPlugin.class);

        // The nexus publish plugin MUST be applied to the root project only, it'll throw an exception otherwise
        if (target == target.getRootProject()) {
            target.getPlugins().apply(ChecksumPlugin.class);
            target.getPlugins().apply(NexusPublishPlugin.class);
            target.getPlugins().apply(OpenApiMergerPlugin.class);
            target.getPlugins().apply(ModuleNamesPlugin.class);
            target.getPlugins().apply(DependencyAnalysisPlugin.class);
        }
    }

    @Override
    public void apply(Project target) {
        defineCapabilities(target);
    }

}
