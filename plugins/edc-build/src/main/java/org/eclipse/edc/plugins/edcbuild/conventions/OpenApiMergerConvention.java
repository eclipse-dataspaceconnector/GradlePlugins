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

package org.eclipse.edc.plugins.edcbuild.conventions;

import com.rameshkp.openapi.merger.gradle.extensions.OpenApiMergerExtension;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Project;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;
import static org.eclipse.edc.plugins.edcbuild.conventions.SwaggerConvention.defaultOutputDirectory;

/**
 * Configures inputs and outputs for the OpenAPI merger plugin
 */
class OpenApiMergerConvention implements EdcConvention {

    private static final String OPEN_API_VERSION = "3.0.1";

    @Override
    public void apply(Project target) {
        if (target == target.getRootProject()) {
            var mergerExt = requireExtension(target, OpenApiMergerExtension.class);
            var buildExtension = requireExtension(target, BuildExtension.class);
            var swaggerExt = buildExtension.getSwagger();

            // the output of the swagger generator serves as input for the merger
            var inputDirectory = swaggerExt.getOutputDirectory().orElse(defaultOutputDirectory(target).toFile());
            mergerExt.getInputDirectory().set(inputDirectory.get());

            mergerExt.output(outputExtension -> {
                // by default use parent directory of the input
                outputExtension.getDirectory().set(inputDirectory.get().getParentFile());
                outputExtension.getFileExtension().set("yaml");
                outputExtension.getFileName().set("openapi");
            });

            mergerExt.openApi(openApi -> {
                openApi.getOpenApiVersion().set(OPEN_API_VERSION);
                openApi.info(info -> {
                    info.getTitle().set(swaggerExt.getTitle());
                    info.getDescription().set(swaggerExt.getDescription());
                    info.getVersion().set(target.getVersion().toString());
                    info.license(license -> {
                        var mavenPomExt = buildExtension.getPom();
                        license.getName().set(mavenPomExt.getLicenseName());
                        license.getUrl().set(mavenPomExt.getLicenseUrl());
                    });
                });
            });
        }
    }
}
