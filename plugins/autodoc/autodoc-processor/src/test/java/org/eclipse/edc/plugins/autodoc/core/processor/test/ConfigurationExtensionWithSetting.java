/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.plugins.autodoc.core.processor.test;

import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ConfigurationExtension;
import org.eclipse.edc.spi.system.configuration.Config;

public class ConfigurationExtensionWithSetting implements ConfigurationExtension {

    @Setting("Valid because this is an extension of ServiceExtension")
    private static final String VALID_SETTING = "any";

    @Override
    public Config getConfig() {
        return null;
    }
}
