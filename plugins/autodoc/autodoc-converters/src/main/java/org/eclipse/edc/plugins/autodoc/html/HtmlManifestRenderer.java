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

import j2html.TagCreator;
import j2html.tags.Text;
import j2html.tags.specialized.ArticleTag;
import j2html.tags.specialized.CodeTag;
import j2html.tags.specialized.HeadTag;
import j2html.tags.specialized.UlTag;
import org.eclipse.edc.plugins.autodoc.spi.ManifestConverterException;
import org.eclipse.edc.plugins.autodoc.spi.ManifestRenderer;
import org.eclipse.edc.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.edc.runtime.metamodel.domain.ModuleType;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.eclipse.edc.runtime.metamodel.domain.ServiceReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static j2html.TagCreator.a;
import static j2html.TagCreator.article;
import static j2html.TagCreator.b;
import static j2html.TagCreator.body;
import static j2html.TagCreator.code;
import static j2html.TagCreator.em;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h3;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.head;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.li;
import static j2html.TagCreator.nav;
import static j2html.TagCreator.section;
import static j2html.TagCreator.span;
import static j2html.TagCreator.style;
import static j2html.TagCreator.table;
import static j2html.TagCreator.tbody;
import static j2html.TagCreator.td;
import static j2html.TagCreator.th;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.tr;
import static j2html.TagCreator.ul;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;


public class HtmlManifestRenderer implements ManifestRenderer {

    private final OutputStream outputStream;
    private final HeadTag head = head();
    private final UlTag menu = ul();
    private final ArticleTag content = article();

    public HtmlManifestRenderer(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void renderDocumentHeader() {
        head.with(style("""
                * {
                  box-sizing: border-box;
                }
                nav {
                  float: left;
                  width: 20%;
                  padding: 20px;
                }
                article {
                  float: left;
                  padding: 20px;
                  width: 80%;
                }
                table {
                  margin: 1em 0;
                  border-collapse: collapse;
                  width: 100%;
                  overflow-x: auto;
                  display: block;
                  font-variant-numeric: lining-nums tabular-nums;
                }
                tbody {
                  margin-top: 0.5em;
                  border-top: 1px solid #1a1a1a;
                  border-bottom: 1px solid #1a1a1a;
                }
                td {
                    padding: 5px
                }
                .odd {
                  background-color: cccccc;
                }
                .even {
                  background-color: eeeeee;
                }
                """));
    }

    @Override
    public void renderModuleHeading(@Nullable String moduleName, @NotNull String modulePath, @NotNull String version) {
        var modulePublishedName = modulePath.split(":")[1];

        var ul = ul();
        if (moduleName != null) {
            ul.with(li(b("Name: "), new Text(moduleName)));
        }
        ul.with(
                li(b("Path: "), code(modulePath)),
                li(b("Version: "), code(version))
        );

        var moduleTitle = h2(modulePublishedName).withId(modulePublishedName);
        content.with(hr())
                .with(a(moduleTitle).withHref("#" + modulePublishedName))
                .with(ul);

        menu.with(li().with(a(modulePublishedName).withHref("#" + modulePublishedName)));
    }

    @Override
    public void renderCategories(List<String> categories) {
        var categoriesString = categories.isEmpty()
                ? "none"
                : categories.stream().filter(Objects::nonNull).filter(it -> !it.isBlank()).collect(joining(", "));

        content.with(em("Categories: " + categoriesString));
    }

    @Override
    public void renderExtensionPoints(List<Service> extensionPoints) {
        content.with(h3("Extension Points"));

        if (extensionPoints.isEmpty()) {
            content.with(em("none"));
        } else {
            var list = ul();
            extensionPoints.stream().map(it -> li(em(it.getService()))).forEach(list::with);
            content.with(list);
        }
    }

    @Override
    public void renderExtensionHeading() {

    }

    @Override
    public void renderExtensionHeader(@NotNull String className, @Nullable String name, @Nullable String overview, ModuleType type) {
        var extensionDetail = ul()
                .with(li(b("Class: ")).with(code(className)))
                .with(li(b("Type: ")).with(span(type.getKey())));

        if (overview != null) {
            extensionDetail.with(li(b("Overview: ")).with(span(overview)));
        }

        content.with(h3("Extension " + name)).with(extensionDetail);
    }

    @Override
    public void renderConfigurations(List<ConfigurationSetting> configuration) {
        content.with(h4("Configuration: "));
        if (configuration.isEmpty()) {
            content.with(em("none"));
        } else {
            var header = tr(th("Key"), th("Required"), th("Type"), th("Default"),
                    th("Pattern"), th("Min"), th("Max"), th("Description"));

            var tbody = tbody();

            range(0, configuration.size()).mapToObj(index -> {
                var setting = configuration.get(index);
                var clazz = index % 2 == 0 ? "even" : "odd";
                return tr(
                        td(code(setting.getKey())),
                        td(setting.isRequired() ? code("x") : null).attr("align", "center"),
                        td(code(setting.getType())).attr("align", "center"),
                        td(code(setting.getDefaultValue())),
                        td(codeOrNull(setting.getPattern())).attr("align", "center"),
                        td(codeOrNull(setting.getMinimum())).attr("align", "right"),
                        td(codeOrNull(setting.getMaximum())).attr("align", "right"),
                        td(setting.getDescription()).attr("width", "40%")
                ).withClass(clazz);
            }).forEach(tbody::with);

            content.with(table().with(thead(header)).with(tbody));
        }
    }

    @Override
    public void renderProvidedServices(List<Service> provides) {
        content.with(h4("Provided Services: "));

        if (provides.isEmpty()) {
            content.with(em("none"));
        } else {
            var ul = ul();
            provides.stream().map(Service::getService)
                    .map(TagCreator::code)
                    .map(TagCreator::li)
                    .forEach(ul::with);
            content.with(ul);
        }
    }

    @Override
    public void renderReferencedServices(List<ServiceReference> references) {
        content.with(h4(new Text("Referenced (injected) Services ("), em("emphasized if required"), new Text("): ")));

        if (references.isEmpty()) {
            content.with(em("none"));
        } else {
            var ul = ul();
            references.stream()
                    .map(service -> {
                        var code = code(service.getService());
                        return service.isRequired() ? em(code) : code;
                    })
                    .map(TagCreator::li)
                    .forEach(ul::with);
            content.with(ul);
        }
    }

    @Override
    public OutputStream finalizeRendering() {
        try {
            var html = html()
                    .with(head)
                    .with(body().with(section(nav().with(menu), content)));
            outputStream.write(html.render().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ManifestConverterException(e);
        }
        return outputStream;
    }

    @Nullable
    private CodeTag codeOrNull(Long value) {
        return Optional.ofNullable(value).map(Object::toString).map(TagCreator::code).orElse(null);
    }

    @Nullable
    private CodeTag codeOrNull(String value) {
        return Optional.ofNullable(value).map(TagCreator::code).orElse(null);
    }
}
