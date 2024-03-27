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

package org.eclipse.edc.plugins.autodoc.core.processor.test;

import org.eclipse.edc.runtime.metamodel.annotation.Setting;

public class NotExtensionWithSetting {
    @Setting("the setting must stay in a ServiceExtension class")
    private static final String UNEXPECTED_SETTING = "any";
}
