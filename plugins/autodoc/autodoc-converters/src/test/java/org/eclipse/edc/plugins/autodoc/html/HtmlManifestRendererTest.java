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

package org.eclipse.edc.plugins.autodoc.html;

import org.eclipse.edc.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.eclipse.edc.runtime.metamodel.domain.ServiceReference;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.edc.runtime.metamodel.domain.ModuleType.EXTENSION;

public class HtmlManifestRendererTest {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final HtmlManifestRenderer renderer = new HtmlManifestRenderer(outputStream);

    @Test
    void shouldOutputValidHtml() {
        var output = renderer.finalizeRendering();

        assertThat(output).asString().satisfies(html -> {
            assertThat(html).startsWith("<html>");
            assertThat(html).endsWith("</html>");
        });
    }

    @Nested
    class ModuleHeading {
        @Test
        void shouldRenderHeadingBlock_whenNameIsNull() {
            renderer.renderModuleHeading(null, "module:path", "0.2.0");

            var output = renderer.finalizeRendering();

            assertThat(output).asString()
                    .contains("<li><a href=\"#path\">path</a></li>")
                    .contains("<h2 id=\"path\">path</h2>")
                    .contains("<li><b>Path: </b><code>module:path</code></li>")
                    .contains("<li><b>Version: </b><code>0.2.0</code></li>");
        }

        @Test
        void shouldRenderName_whenItIsNotNull() {
            renderer.renderModuleHeading("moduleName", "module:path", "0.2.0");

            var output = renderer.finalizeRendering();

            assertThat(output).asString().contains("<li><b>Name: </b>moduleName</li>");
        }
    }

    @Nested
    class Categories {
        @Test
        void shouldRenderCategoriesAsItalic() {
            renderer.renderCategories(List.of("category1", "category2"));

            var output = renderer.finalizeRendering();

            assertThat(output).asString().contains("<em>Categories: category1, category2</em>");
        }

        @Test
        void shouldFilterOutNullOrEmptyCategories() {
            renderer.renderCategories(List.of("category", " "));

            var output = renderer.finalizeRendering();

            assertThat(output).asString().contains("<em>Categories: category</em>");
        }

        @Test
        void shouldRenderNone_whenNoCategories() {
            renderer.renderCategories(emptyList());

            var output = renderer.finalizeRendering();

            assertThat(output).asString().contains("<em>Categories: none</em>");
        }
    }

    @Nested
    class ExtensionPoints {
        @Test
        void shouldRenderExtensionPointsAsListItems() {
            renderer.renderExtensionPoints(List.of(new Service("extensionPoint")));

            var output = renderer.finalizeRendering();

            assertThat(output).asString()
                    .contains("<h3>Extension Points</h3>")
                    .contains("<li><em>extensionPoint</em></li>");
        }

        @Test
        void shouldRenderNoneIfNoExtensionPoints() {
            renderer.renderExtensionPoints(emptyList());

            var output = renderer.finalizeRendering();

            assertThat(output).asString()
                    .contains("<h3>Extension Points</h3>")
                    .contains("<em>none</em>");
        }
    }

    @Nested
    class ExtensionHeader {
        @Test
        void shouldRenderExtensionsHeading() {
            renderer.renderExtensionHeader("ClassName", "Human Readable Name", "this extension rocks!", EXTENSION);

            var output = renderer.finalizeRendering();

            assertThat(output).asString()
                    .contains("<h3>Extension Human Readable Name</h3>")
                    .contains("<b>Class: </b><code>ClassName</code>")
                    .contains("<b>Type: </b><span>extension</span>")
                    .contains("<b>Overview: </b><span>this extension rocks!</span>");
        }

        @Test
        void shouldNotRenderOverview_whenItIsNull() {
            renderer.renderExtensionHeader("ClassName", "Human Readable Name", null, EXTENSION);

            var output = renderer.finalizeRendering();

            assertThat(output).asString()
                    .doesNotContain("Overview");
        }
    }

    @Nested
    class Configurations {
        @Test
        void shouldRenderNone_whenNoConfigurations() {
            renderer.renderConfigurations(emptyList());

            var output = renderer.finalizeRendering();

            assertThat(output).asString().contains("<h4>Configuration: </h4><em>none</em>");
        }

        @Test
        void shouldRenderTableWithConfiguration() {
            var setting = ConfigurationSetting.Builder.newInstance()
                    .key("edc.setting.path")
                    .required(true)
                    .build();
            renderer.renderConfigurations(List.of(setting));

            var output = renderer.finalizeRendering();

            assertThat(output).asString()
                    .contains("<table>", "<thead>", "<tbody>", "<tr>", "<td>")
                    .contains("<td><code>edc.setting.path</code></td>");
        }
    }

    @Nested
    class ProvidedServices {
        @Test
        void shouldRenderNone_whenNoProvidedServices() {
            renderer.renderProvidedServices(emptyList());

            var output = renderer.finalizeRendering();

            assertThat(output).asString().contains("<h4>Provided Services: </h4><em>none</em>");
        }

        @Test
        void shouldRenderProvidedServices() {
            renderer.renderProvidedServices(List.of(new Service("providedService")));

            var output = renderer.finalizeRendering();

            assertThat(output).asString().contains("<h4>Provided Services: </h4><ul><li><code>providedService</code></li></ul>");
        }
    }

    @Nested
    class ReferencedServices {
        @Test
        void shouldRenderNone_whenNoProvidedServices() {
            renderer.renderReferencedServices(emptyList());

            var output = renderer.finalizeRendering();

            assertThat(output).asString().contains("<h4>Referenced (injected) Services (<em>emphasized if required</em>): </h4><em>none</em>");
        }

        @Test
        void shouldRenderProvidedServices() {
            renderer.renderReferencedServices(List.of(
                    new ServiceReference("requiredService", true),
                    new ServiceReference("notRequiredService", false)
            ));

            var output = renderer.finalizeRendering();

            assertThat(output).asString()
                    .contains("<li><em><code>requiredService</code></em></li>")
                    .contains("<li><code>notRequiredService</code></li>");
        }
    }

}
