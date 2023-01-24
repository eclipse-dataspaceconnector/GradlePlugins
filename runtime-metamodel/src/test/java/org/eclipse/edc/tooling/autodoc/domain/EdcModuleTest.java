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

package org.eclipse.edc.tooling.autodoc.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.edc.runtime.metamodel.domain.EdcModule;
import org.eclipse.edc.runtime.metamodel.domain.EdcServiceExtension;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.eclipse.edc.runtime.metamodel.domain.ServiceReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EdcModuleTest {

    @Test
    void verifySerializeDeserialize() throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var module = EdcModule.Builder.newInstance()
                .modulePath("foo:bar")
                .version("1.0.0")
                .extension(EdcServiceExtension.Builder.newInstance()
                        .name("test")
                        .overview("overview")
                        .categories(List.of("category"))
                        .className("test-classname")
                        .provides(List.of(new Service("com.bar.BazService")))
                        .references(List.of(new ServiceReference("com.bar.QuuxService", false)))
                        .configuration(List.of(ConfigurationSetting.Builder.newInstance().key("key1").build()))
                        .build())
                .extensionPoints(List.of(new Service("com.bar.BarService")))
                .build();

        var serialized = mapper.writeValueAsString(module);
        var deserialized = mapper.readValue(serialized, EdcModule.class);

        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getExtensionPoints().size()).isEqualTo(1);

        var extension = deserialized.getExtensions().iterator().next();
        assertThat(extension.getCategories().size()).isEqualTo(1);
        assertThat(extension.getProvides().size()).isEqualTo(1);
        assertThat(extension.getReferences().size()).isEqualTo(1);
        assertThat(extension.getConfiguration().size()).isEqualTo(1);
    }
}
