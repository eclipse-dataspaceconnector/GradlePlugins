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

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;
import static org.eclipse.edc.plugins.edcbuild.conventions.Repositories.releaseRepo;
import static org.eclipse.edc.plugins.edcbuild.conventions.Repositories.snapshotRepo;

/**
 * Configures the Maven repos for publishing depending on the project's version
 */
class MavenPublishingConvention implements EdcConvention {


    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    @Override
    public void apply(Project target) {
        if (target.hasProperty("skip.signing")) {
            return;
        }
        var pubExt = requireExtension(target, PublishingExtension.class);

        if (pubExt.getRepositories().stream().noneMatch(repo -> repo.getName().equals(Repositories.REPO_NAME_SONATYPE) && repo instanceof MavenArtifactRepository)) {

            if (target.getVersion().toString().endsWith(SNAPSHOT_SUFFIX)) {
                pubExt.repositories(snapshotRepo());
            } else {
                pubExt.repositories(releaseRepo());
            }
        }
    }


}
