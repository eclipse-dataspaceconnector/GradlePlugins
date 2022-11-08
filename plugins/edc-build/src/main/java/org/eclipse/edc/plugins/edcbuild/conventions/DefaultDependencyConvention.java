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

import org.eclipse.edc.plugins.edcbuild.extensions.BuildExtension;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;

import static java.lang.String.format;
import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Applies default dependencies to all "java-library" projects, i.e. Jackson, the runtime-metamodel, JUnit, Mockito and AssertJ in
 * their respective configurations.
 */
class DefaultDependencyConvention implements EdcConvention {

    private static final String EDC_GROUP_ID = "org.eclipse.dataspaceconnector";

    @Override
    public void apply(Project target) {
        target.getPluginManager().withPlugin("java-library", plugin -> {
            var ext = requireExtension(target, BuildExtension.class).getVersions();
            var group = EDC_GROUP_ID;
            target.setGroup(group);
            target.setVersion(ext.getProjectVersion().getOrElse("0.0.1-SNAPSHOT"));

            // classpath dependencies
            var d = target.getDependencies();
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("org.jetbrains:annotations:%s", ext.getJetbrainsAnnotation().getOrElse("15.0")));
            var jacksonVersion = ext.getJackson().getOrElse("2.14.0-rc2");
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("com.fasterxml.jackson.core:jackson-core:%s", jacksonVersion));
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("com.fasterxml.jackson.core:jackson-annotations:%s", jacksonVersion));
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("com.fasterxml.jackson.core:jackson-databind:%s", jacksonVersion));
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:%s", jacksonVersion));
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("%s:runtime-metamodel:%s", group, ext.getMetaModel().getOrElse("0.0.1-SNAPSHOT")));

            //test classpath dependencies
            var jupiterVersion = ext.getJupiter().getOrElse("5.8.2");
            d.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-api:%s", jupiterVersion));
            d.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-params:%s", jupiterVersion));
            d.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-engine:%s", jupiterVersion));
            d.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.mockito:mockito-core:%s", ext.getMockito().getOrElse("4.2.0")));
            d.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.assertj:assertj-core:%s", ext.getAssertJ().getOrElse("3.22.0")));
        });
    }
}
