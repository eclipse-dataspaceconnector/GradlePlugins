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
import org.eclipse.edc.plugins.edcbuild.Versions;
import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.eclipse.edc.plugins.edcbuild.tasks.PrintApiGroupTask;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;
import static org.eclipse.edc.plugins.edcbuild.conventions.SwaggerConvention.defaultOutputDirectory;
import static org.gradle.api.plugins.JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME;

/**
 * Configures the Swagger Resolve task to create openapi yaml file per project
 */
class SwaggerResolveConvention implements EdcConvention {

    private static final String DEFAULT_API_GROUP = "";
    public static final String SWAGGER_GRADLE_PLUGIN = "io.swagger.core.v3.swagger-gradle-plugin";

    @Override
    public void apply(Project target) {
        target.getPluginManager().withPlugin(SWAGGER_GRADLE_PLUGIN, appliedPlugin -> {

            target.getTasks().register("apiGroups", PrintApiGroupTask.class);

            Stream.of(
                    "io.swagger.core.v3:swagger-jaxrs2-jakarta:%s".formatted(Versions.SWAGGER),
                    "jakarta.ws.rs:jakarta.ws.rs-api:%s".formatted(Versions.JAKARTA_WS_RS)
            ).forEach(dependency -> target.getDependencies().add(IMPLEMENTATION_CONFIGURATION_NAME, dependency));

            var classpath = requireExtension(target, JavaPluginExtension.class)
                    .getSourceSets().getAt("main").getRuntimeClasspath();
            var swaggerExt = requireExtension(target, BuildExtension.class).getSwagger();

            var resourcePkgs = swaggerExt.getResourcePackages(); // already provides the default

            target.getTasks().withType(ResolveTask.class, task -> {
                var outputFileName = swaggerExt.getOutputFilename().getOrElse(target.getName());
                var fallbackOutputDir = defaultOutputDirectory(target);
                var apiGroup = swaggerExt.getApiGroup().getOrElse(DEFAULT_API_GROUP);

                var outputDir = Path.of(swaggerExt.getOutputDirectory().getOrElse(fallbackOutputDir.toFile()).toURI())
                        .resolve(apiGroup)
                        .toFile();

                task.setOutputFileName(outputFileName);
                task.setOutputDir(outputDir);
                task.setOutputFormat(ResolveTask.Format.YAML);
                task.setSortOutput(true);
                task.setPrettyPrint(true);
                task.setClasspath(classpath);
                task.setBuildClasspath(task.getClasspath());
                task.setResourcePackages(resourcePkgs);
            });

            target.getTasks().register("openapi", ResolveTask.class).configure(task -> {
                var outputDir = target.getLayout().getBuildDirectory().getAsFile().get().toPath()
                        .resolve("docs").resolve("openapi")
                        .toFile();

                target.getTasks().findByName("jar").dependsOn(task);
                task.setGroup("documentation");
                task.setDescription("Generates openapi specification documentation.");
                task.setOutputFileName(swaggerExt.getApiGroup().getOrElse("openapi"));
                task.setOutputDir(outputDir);
                task.setOutputFormat(ResolveTask.Format.YAML);
                task.setSortOutput(true);
                task.setPrettyPrint(true);
                task.setClasspath(classpath);
                task.setBuildClasspath(task.getClasspath());
                task.setResourcePackages(resourcePkgs);
            });

            target.getConfigurations().all(c -> c.exclude(Map.of("group", "com.fasterxml.jackson.jaxrs",
                    "module", "jackson-jaxrs-json-provider")));
        });
    }
}
