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

import io.swagger.v3.plugins.gradle.tasks.ResolveTask;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.hidetake.gradle.swagger.generator.SwaggerGeneratorPlugin;

import java.util.Map;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;
import static org.eclipse.edc.plugins.edcbuild.conventions.SwaggerConvention.defaultOutputDirectory;

/**
 * Congfigures the Swagger Generator to create openapi yaml file per project
 */
class SwaggerGeneratorConvention implements EdcConvention {
    @Override
    public void apply(Project target) {
        // apply root script plugin
        if (target == target.getRootProject()) {
            target.getPlugins().apply(SwaggerGeneratorPlugin.class);
        }

        target.getPluginManager().withPlugin("io.swagger.core.v3.swagger-gradle-plugin", appliedPlugin -> {
            target.getDependencies().add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
                    "io.swagger.core.v3:swagger-jaxrs2-jakarta:2.2.2");
            target.getDependencies().add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
                    "jakarta.ws.rs:jakarta.ws.rs-api:3.1.0");


            var javaExt = requireExtension(target, JavaPluginExtension.class);
            var swaggerExt = requireExtension(target, BuildExtension.class).getSwagger();
            var outputPath = defaultOutputDirectory(target);

            var outputFileName = swaggerExt.getOutputFilename().getOrElse(target.getName());
            var outputDir = swaggerExt.getOutputDirectory().getOrElse(outputPath.toFile());
            var resourcePkgs = swaggerExt.getResourcePackages(); // already provides the default

            target.getTasks().withType(ResolveTask.class, task -> {
                task.setOutputFileName(outputFileName);
                task.setOutputDir(outputDir);
                task.setOutputFormat(ResolveTask.Format.YAML);
                task.setSortOutput(true);
                task.setPrettyPrint(true);
                task.setClasspath(javaExt.getSourceSets().getAt("main").getRuntimeClasspath());
                task.setBuildClasspath(task.getClasspath());
                task.setResourcePackages(resourcePkgs);
            });

            target.getConfigurations().all(c -> c.exclude(Map.of("group", "com.fasterxml.jackson.jaxrs",
                    "module", "jackson-jaxrs-json-provider")));
        });
    }
}
