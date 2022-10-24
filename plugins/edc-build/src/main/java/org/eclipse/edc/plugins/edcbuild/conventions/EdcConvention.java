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

/**
 * Applies the "convention over configuration" pattern as specified by the Gradle Documentation
 *
 * @see <a href="https://docs.gradle.org/current/userguide/designing_gradle_plugins.html#architecture">Gradle Documentation</a>
 */
@FunctionalInterface
public interface EdcConvention {
    /**
     * Applies the convention to the given target. Typically, conventions are default values for configuration.
     *
     * @param target The project to which the convention applies
     */
    void apply(Project target);
}
