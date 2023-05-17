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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.publish.PublishingExtension;

import java.util.Optional;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Configures the Maven repos for publishing depending on the project's version
 */
class MavenPublishingConvention implements EdcConvention {


    @Override
    public void apply(Project target) {
        if (target.hasProperty("skip.signing")) {
            return;
        }
        var pubExt = requireExtension(target, PublishingExtension.class);

        Optional.ofNullable(getRepoOverride())
                .ifPresent(pubExt::repositories);
    }

    private Action<? super RepositoryHandler> getRepoOverride() {
        var repoUrl = System.getProperty("edc.publish.url");
        var repoName = System.getProperty("edc.publish.repoName");
        var repoUser = System.getProperty("edc.publish.user");
        var repoPwd = System.getProperty("edc.publish.password");


        if (repoUrl == null || repoName == null) return null;

        return handler -> handler.maven(repo -> {
            repo.setUrl(repoUrl);
            repo.credentials(cred -> {
                cred.setUsername(repoUser);
                cred.setPassword(repoPwd);
            });
            repo.setAllowInsecureProtocol(repoUrl.startsWith("http://"));
            repo.setName(repoName);
        });
    }


}
