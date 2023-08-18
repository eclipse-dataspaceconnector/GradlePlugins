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
 *       Fraunhofer Institute for Software and Systems Engineering - add maven publication
 *
 */

package org.eclipse.edc.plugins.edcbuild.conventions;

/**
 * Contains statically accessible {@link EdcConvention} objects that can be applied to a project.
 */
public class Conventions {
    public static EdcConvention checkstyle() {
        return new CheckstyleConvention();
    }


    public static EdcConvention mavenPublishing() {
        return new MavenPublishingConvention();
    }

    public static EdcConvention signing() {
        return new SigningConvention();
    }

    public static EdcConvention repositories() {
        return new RepositoriesConvention();
    }

    public static EdcConvention defaultDependencies() {
        return new DefaultDependencyConvention();
    }

    public static EdcConvention mavenPom() {
        return new MavenArtifactConvention();
    }

    public static EdcConvention jacoco() {
        return new JacocoConvention();
    }

    public static EdcConvention java() {
        return new JavaConvention();
    }

    public static EdcConvention allDependencies() {
        return new AllDependenciesConvention();
    }

    public static EdcConvention tests() {
        return new TestConvention();
    }

    public static EdcConvention jar() {
        return new JarConvention();
    }

    public static EdcConvention nexusPublishing() {
        return new NexusPublishingConvention();
    }

    public static EdcConvention rootBuildScript() {
        return new RootBuildScriptConvention();
    }

    public static EdcConvention swagger() {
        return new SwaggerConvention();
    }

    public static EdcConvention swaggerGenerator() {
        return new SwaggerGeneratorConvention();
    }

    public static EdcConvention openApiMerger() {
        return new OpenApiMergerConvention();
    }
    
    public static EdcConvention mavenPublication() {
        return new MavenPublicationConvention();
    }
}
