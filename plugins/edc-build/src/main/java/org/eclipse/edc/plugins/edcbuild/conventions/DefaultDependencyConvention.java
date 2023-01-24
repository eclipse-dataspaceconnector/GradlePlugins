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
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import org.gradle.api.plugins.JavaPlugin;

import static java.lang.String.format;
import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Applies default dependencies to all "java-library" projects, i.e. Jackson, the runtime-metamodel, JUnit, Mockito and AssertJ in
 * their respective configurations.
 */
class DefaultDependencyConvention implements EdcConvention {

    private static final String EDC_GROUP_ID = "org.eclipse.edc";

    @Override
    public void apply(Project target) {
        target.getPluginManager().withPlugin("java-library", plugin -> {


            var ext = requireExtension(target, BuildExtension.class).getVersions();
            var catalogReader = new CatalogReader(target, ext.getCatalogName());
            var group = EDC_GROUP_ID;
            target.setGroup(group);
            target.setVersion(ext.getProjectVersion().getOrElse("0.0.1-SNAPSHOT"));

            // classpath dependencies
            var d = target.getDependencies();
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("org.jetbrains:annotations:%s", ext.getJetbrainsAnnotation().getOrElse(catalogReader.versionFor("jetbrainsAnnotation", "15.0"))));
            var jacksonVersion = ext.getJackson().getOrElse(catalogReader.versionFor("jackson", "2.14.0-rc2"));
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("com.fasterxml.jackson.core:jackson-core:%s", jacksonVersion));
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("com.fasterxml.jackson.core:jackson-annotations:%s", jacksonVersion));
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("com.fasterxml.jackson.core:jackson-databind:%s", jacksonVersion));
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:%s", jacksonVersion));
            d.add(JavaPlugin.API_CONFIGURATION_NAME, format("%s:runtime-metamodel:%s", group, ext.getMetaModel().getOrElse("0.0.1-SNAPSHOT")));

            //test classpath dependencies
            var jupiterVersion = ext.getJupiter().getOrElse(catalogReader.versionFor("jupiter", "5.8.2"));
            d.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-api:%s", jupiterVersion));
            d.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-params:%s", jupiterVersion));
            d.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-engine:%s", jupiterVersion));
            d.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.mockito:mockito-core:%s", ext.getMockito().getOrElse(catalogReader.versionFor("mockito", "4.2.0"))));
            d.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.assertj:assertj-core:%s", ext.getAssertJ().getOrElse(catalogReader.versionFor("assertj", "3.22.0"))));
        });
    }


    private static class CatalogReader {
        private static final String FIELDNAME_CONFIG = "config";
        private final Project target;
        private final String catalogName;

        CatalogReader(Project target, String catalogName) {
            this.target = target;
            this.catalogName = catalogName;
        }

        String versionFor(String versionRef, String defaultValue) {
            var factory = target.getExtensions().findByName(catalogName);
            if (factory == null) {
                target.getLogger().debug("No VersionCatalog with name {} found. Please either override the version for {} in your build script, or instantiate the version catalog in your client project.", catalogName, versionRef);
                return defaultValue;
            }
            try {
                var field = AbstractExternalDependencyFactory.class.getDeclaredField(FIELDNAME_CONFIG);
                field.setAccessible(true);
                var catalog = (DefaultVersionCatalog) field.get(factory);
                var versionModel = catalog.getVersion(versionRef);
                return versionModel.getVersion().getRequiredVersion();
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new GradleException("error introspecting catalog", e);
            }
        }
    }
}
