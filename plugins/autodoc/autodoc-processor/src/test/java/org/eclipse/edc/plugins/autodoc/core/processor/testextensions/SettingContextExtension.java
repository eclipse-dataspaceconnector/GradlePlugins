/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

package org.eclipse.edc.plugins.autodoc.core.processor.testextensions;

import org.eclipse.edc.plugins.autodoc.core.processor.Constants;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.SettingContext;
import org.eclipse.edc.spi.system.ServiceExtension;

import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_CLASS_PREFIX_SETTING_KEY;
import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_FIELD_PREFIX_SETTING_KEY;


@SettingContext(TEST_CLASS_PREFIX_SETTING_KEY)
public class SettingContextExtension implements ServiceExtension {

    @Setting(context = TEST_FIELD_PREFIX_SETTING_KEY)
    private static final String TEST_FIELD_PREFIX_SETTING = Constants.TEST_SETTING_ID_KEY;

    @Setting
    private static final String TEST_CLASS_PREFIX_SETTING = Constants.TEST_SETTING_ID_KEY;

}
