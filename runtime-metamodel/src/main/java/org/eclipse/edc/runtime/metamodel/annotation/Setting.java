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
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - improvements
 *
 */

package org.eclipse.edc.runtime.metamodel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a runtime configuration setting.
 */
@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Setting {

    /**
     * The setting description.
     */
    String value() default "";

    String type() default "string";

    /**
     * The setting default value. Empty string if no default value is provided
     *
     * @return the setting's default value
     */
    String defaultValue() default "";

    long min() default Long.MIN_VALUE;

    long max() default Long.MAX_VALUE;

    /**
     * Returns true if the setting is required.
     */
    boolean required() default false;

}
