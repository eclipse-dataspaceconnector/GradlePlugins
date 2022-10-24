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

package org.eclipse.edc.plugins.edcbuild.extensions;

import org.gradle.api.provider.Property;

public abstract class VersionsExtension {
    public abstract Property<String> getProjectVersion();

    public abstract Property<String> getJetbrainsAnnotation();

    public abstract Property<String> getJackson();

    public abstract Property<String> getMetaModel();

    public abstract Property<String> getJupiter();

    public abstract Property<String> getMockito();

    public abstract Property<String> getAssertJ();

}
