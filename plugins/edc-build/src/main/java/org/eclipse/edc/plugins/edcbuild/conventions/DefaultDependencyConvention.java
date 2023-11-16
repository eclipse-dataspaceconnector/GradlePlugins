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
import org.gradle.api.artifacts.VersionConstraint;
import org.gradle.api.internal.catalog.AbstractExternalDependencyFactory;
import org.gradle.api.internal.catalog.DefaultVersionCatalog;
import org.gradle.api.internal.catalog.VersionModel;
import org.gradle.api.plugins.JavaPlugin;

import java.util.Optional;

import static java.lang.String.format;
import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;
import static org.gradle.api.plugins.JavaPlugin.API_CONFIGURATION_NAME;
import static org.gradle.api.plugins.JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME;
import static org.gradle.api.plugins.JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME;

/**
 * Applies default dependencies to all "java-library" projects, i.e. Jackson, the runtime-metamodel, JUnit, Mockito and AssertJ in
 * their respective configurations.
 */
class DefaultDependencyConvention implements EdcConvention {

    private static final String EDC_GROUP_ID = "org.eclipse.edc";
    private static final String DEFAULT_JETBRAINS_ANNOTATION_VERSION = "24.0.1";
    private static final String DEFAULT_JACKSON_VERSION = "2.14.2";
    private static final String DEFAULT_METAMODEL_VERSION = "0.3.2-SNAPSHOT";
    private static final String DEFAULT_MOCKITO_VERSION = "5.2.0";
    private static final String DEFAULT_ASSERTJ_VERSION = "3.23.1";
    private static final String DEFAULT_JUPITER_VERSION = "5.9.2";

    @Override
    public void apply(Project target) {
        target.getPluginManager().withPlugin("java-library", plugin -> {

            var ext = requireExtension(target, BuildExtension.class).getVersions();
            var catalogReader = new CatalogReader(target, ext.getCatalogName());
            var d = target.getDependencies();

            // classpath dependencies
            var jetbrainsAnnotationVersion = ext.getJetbrainsAnnotation().getOrElse(catalogReader.versionFor("jetbrainsAnnotation", DEFAULT_JETBRAINS_ANNOTATION_VERSION));
            var jacksonVersion = ext.getJackson().getOrElse(catalogReader.versionFor("jackson", DEFAULT_JACKSON_VERSION));
            var metamodelVersion = ext.getMetaModel().getOrElse(DEFAULT_METAMODEL_VERSION);
            d.add(API_CONFIGURATION_NAME, format("org.jetbrains:annotations:%s", jetbrainsAnnotationVersion));
            d.add(API_CONFIGURATION_NAME, format("com.fasterxml.jackson.core:jackson-core:%s", jacksonVersion));
            d.add(API_CONFIGURATION_NAME, format("com.fasterxml.jackson.core:jackson-annotations:%s", jacksonVersion));
            d.add(API_CONFIGURATION_NAME, format("com.fasterxml.jackson.core:jackson-databind:%s", jacksonVersion));
            // this is a temporary workaround to compensate for azure libs, that use XmlMapper, but don't correctly list it as a dependency
            if (hasAzureDependency(target)) {
                d.add(JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME, format("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:%s", jacksonVersion));
            }
            d.add(API_CONFIGURATION_NAME, format("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:%s", jacksonVersion));
            d.add(API_CONFIGURATION_NAME, format("%s:runtime-metamodel:%s", EDC_GROUP_ID, metamodelVersion));

            //test classpath dependencies
            var jupiterVersion = ext.getJupiter().getOrElse(catalogReader.versionFor("jupiter", DEFAULT_JUPITER_VERSION));
            var mockitoVersion = ext.getMockito().getOrElse(catalogReader.versionFor("mockito", DEFAULT_MOCKITO_VERSION));
            var assertjVersion = ext.getAssertJ().getOrElse(catalogReader.versionFor("assertj", DEFAULT_ASSERTJ_VERSION));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-api:%s", jupiterVersion));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-params:%s", jupiterVersion));
            d.add(TEST_RUNTIME_ONLY_CONFIGURATION_NAME, format("org.junit.jupiter:junit-jupiter-engine:%s", jupiterVersion));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.mockito:mockito-core:%s", mockitoVersion));
            d.add(TEST_IMPLEMENTATION_CONFIGURATION_NAME, format("org.assertj:assertj-core:%s", assertjVersion));
        });
    }

    private boolean hasAzureDependency(Project target) {
        return target.getConfigurations().stream().flatMap(c -> c.getDependencies().stream())
                .anyMatch(d -> d.getGroup() != null && d.getGroup().contains("azure"));
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
                return Optional.ofNullable(catalog)
                        .map(c -> c.getVersion(versionRef))
                        .map(VersionModel::getVersion)
                        .map(VersionConstraint::getRequiredVersion)
                        .orElse(defaultValue);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new GradleException("error introspecting catalog", e);
            }
        }
    }
}
