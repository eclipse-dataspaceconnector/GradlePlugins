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

import com.autonomousapps.DependencyAnalysisExtension;
import com.autonomousapps.DependencyAnalysisPlugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.diagnostics.DependencyReportTask;

import static java.util.Optional.ofNullable;
import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Provides default configuration for the "dependency-analysis" plugin:
 * <ul>
 *     <li>defines general exclusions</li>
 *     <li>defines exclusions for the "unused dependencies" check</li>
 * </ul>
 */
class DependencyAnalysisConvention implements EdcConvention {
    @Override
    public void apply(Project target) {

        target.getTasks().register("allDependencies", DependencyReportTask.class);

        if (!target.hasProperty("dependency.analysis")) {
            return;
        }


        // the dependency analysis plugin will behave differently for root projects and subprojects.
        // for example, the extension will only be available for the root project
        if (target.getRootProject() == target) {
            target.getPluginManager().apply(DependencyAnalysisPlugin.class);
            var ext = requireExtension(target, DependencyAnalysisExtension.class);

            ext.issues(is -> is.all(a -> {
                a.onAny(i -> {
                    i.severity(ofNullable(target.property("dependency.analysis")).orElse("warn").toString());
                    i.exclude(
                            // dependencies declared at the root level for all modules
                            "org.jetbrains:annotations",
                            "com.fasterxml.jackson.datatype:jackson-datatype-jsr310",
                            "com.fasterxml.jackson.core:jackson-core",
                            "com.fasterxml.jackson.core:jackson-annotations",
                            "com.fasterxml.jackson.core:jackson-databind");
                });
                a.onUnusedDependencies(i -> i.exclude(
                        // dependencies declared at the root level for all modules
                        "org.assertj:assertj-core",
                        "org.junit.jupiter:junit-jupiter-api",
                        "org.junit.jupiter:junit-jupiter-params",
                        "org.mockito:mockito-core",
                        "org.eclipse.edc:runtime-metamodel",
                        "org.eclipse.edc:autodoc-processor"
                ));
                a.onIncorrectConfiguration(i -> i.exclude(
                        // some common dependencies are intentionally exported by core:common:connector-core for simplicity
                        "com.squareup.okhttp3:okhttp",
                        "dev.failsafe:failsafe"
                ));

                a.onUsedTransitiveDependencies(i -> i.severity("ignore"));
            }));
            ext.abi(abi -> abi.exclusions(ex -> ex.excludeAnnotations("io\\.opentelemetry\\.extension\\.annotations\\.WithSpan")
            ));
        }
    }
}
