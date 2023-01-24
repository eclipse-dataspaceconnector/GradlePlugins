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

import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

public class ConventionFunctions {

    /**
     * Supplies a generic {@link GradleException} with the given message.
     */
    public static Supplier<GradleException> gradleException(String message) {
        return () -> new GradleException(message);
    }

    /**
     * Supplies a {@link GradleException} that has a specific message indicating that a particular extension is missing.
     */
    static Supplier<GradleException> extensionException(Class<?> extensionClass) {
        return gradleException(extensionClass.getSimpleName() + " expected but was null");
    }

    public static <A> A requireExtension(Project target, Class<A> extensionClass) {
        return ofNullable(target.getExtensions().findByType(extensionClass))
                .orElseThrow(extensionException(extensionClass));
    }
}
