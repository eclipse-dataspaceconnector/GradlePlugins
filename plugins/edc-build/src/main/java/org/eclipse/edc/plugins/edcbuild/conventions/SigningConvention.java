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
import org.gradle.api.publish.PublishingExtension;
import org.gradle.plugins.signing.SigningExtension;
import org.gradle.plugins.signing.SigningPlugin;

import static org.eclipse.edc.plugins.edcbuild.conventions.ConventionFunctions.requireExtension;

/**
 * Configures the "signing" plugin, sets the publications and declares the use of GPG
 */
class SigningConvention implements EdcConvention {
    @Override
    public void apply(Project target) {
        if (target.hasProperty("skip.signing")) {
            return;
        }
        var pubExt = requireExtension(target, PublishingExtension.class);
        var publications = pubExt.getPublications();

        target.getPlugins().apply(SigningPlugin.class);

        var signExt = requireExtension(target, SigningExtension.class);
        signExt.useGpgCmd();
        signExt.sign(publications);
    }
}
