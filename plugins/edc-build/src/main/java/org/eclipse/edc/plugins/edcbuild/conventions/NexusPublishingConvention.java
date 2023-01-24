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

import io.github.gradlenexus.publishplugin.NexusPublishExtension;
import org.gradle.api.Project;

import static org.eclipse.edc.plugins.edcbuild.conventions.Repositories.sonatypeRepo;

class NexusPublishingConvention implements EdcConvention {
    @Override
    public void apply(Project target) {
        if (target == target.getRootProject()) {
            target.getExtensions().configure(NexusPublishExtension.class, nexusPublishExtension -> {
                nexusPublishExtension.repositories(sonatypeRepo());
            });
        }
    }
}
