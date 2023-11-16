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

package org.eclipse.edc.plugins.autodoc.core.processor.testextensions;

import org.eclipse.edc.plugins.autodoc.core.processor.Constants;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.spi.system.ServiceExtension;

import static org.eclipse.edc.plugins.autodoc.core.processor.Constants.TEST_SETTING_DEFAULT_VALUE;

@Provides(SomeOtherService.class)
public class SampleExtensionWithoutAnnotation implements ServiceExtension {
    @Setting(value = Constants.TEST_SETTING_NAME, required = true, defaultValue = TEST_SETTING_DEFAULT_VALUE)
    public static final String TEST_SETTING = Constants.TEST_SETTING_KEY;

    @Inject
    protected RequiredService requiredService;

    @Inject(required = false)
    protected OptionalService optionalService;

    @Provider
    public SomeService provideSomeService() {
        return null;
    }

}
