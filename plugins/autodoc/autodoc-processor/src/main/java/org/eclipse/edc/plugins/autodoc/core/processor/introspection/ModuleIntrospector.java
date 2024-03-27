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

package org.eclipse.edc.plugins.autodoc.core.processor.introspection;

import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.ExtensionPoint;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.runtime.metamodel.annotation.Requires;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.Spi;
import org.eclipse.edc.runtime.metamodel.domain.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static java.util.stream.Collectors.toList;
import static javax.tools.Diagnostic.Kind.ERROR;
import static org.eclipse.edc.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeStringValues;
import static org.eclipse.edc.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeValue;
import static org.eclipse.edc.plugins.autodoc.core.processor.compiler.AnnotationFunctions.mirrorFor;

/**
 * Contains methods for introspecting the current module using the Java Compiler API.
 */
public class ModuleIntrospector {
    private static final String SERVICE_EXTENSION_NAME = "org.eclipse.edc.spi.system.ServiceExtension";
    private static final String SYSTEM_EXTENSION_NAME = "org.eclipse.edc.spi.system.SystemExtension";
    private final Elements elementUtils;
    private final Types typeUtils;
    private final ProcessingEnvironment processingEnv;

    public ModuleIntrospector(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
    }

    public List<String> getCategories(RoundEnvironment environment) {
        var extensionElement = environment.getElementsAnnotatedWith(Spi.class).iterator().next();
        return attributeStringValues("categories", mirrorFor(Spi.class, extensionElement), elementUtils);
    }

    /**
     * Resolves extension points declared with {@link ExtensionPoint}.
     */
    public List<Service> resolveExtensionPoints(RoundEnvironment environment) {
        return environment.getElementsAnnotatedWith(ExtensionPoint.class).stream()
                .map(element -> new Service(element.asType().toString()))
                .collect(toList());
    }

    public String getModuleName(RoundEnvironment environment) {
        var extensionElement = environment.getElementsAnnotatedWith(Spi.class).iterator().next();
        return attributeValue(String.class, "value", mirrorFor(Spi.class, extensionElement), elementUtils);
    }

    /**
     * Get all {@link Element}s that fulfill any of the following criteria:
     * <ul>
     *     <li>Are annotated with {@link Extension}</li>
     *     <li>Are annotated with {@link Provides}</li>
     *     <li>Are annotated with {@link Requires}</li>
     *     <li>Have one or more fields annotated with {@link Inject}</li>
     *     <li>Have one or more methods annotated with {@link Provider}</li>
     * </ul>
     * <p>
     * Note that elements are pruned, i.e. every extension only occurs once. This is important because extensions that have multiple
     * relevant fields and are annotated, will only occur once in the result.
     *
     * @param environment the {@link RoundEnvironment} that is passed in to the annotation processor
     * @return a set containing the distinct extension symbols. Elements in that set are most likely of type Symbol.ClassSymbol
     */
    public Set<Element> getExtensionElements(RoundEnvironment environment) {
        var settingsSymbols = environment.getElementsAnnotatedWith(Setting.class).stream()
                .peek(setting -> {
                    var enclosingElement = setting.getEnclosingElement().asType();
                    var serviceExtensionType = typeUtils.erasure(elementUtils.getTypeElement(SYSTEM_EXTENSION_NAME).asType());
                    if (!typeUtils.isAssignable(enclosingElement, serviceExtensionType)) {
                        var message = "@Setting annotation must be used inside a ServiceExtension implementation, the " +
                                "ones defined in %s will be excluded from the autodoc manifest".formatted(enclosingElement);
                        processingEnv.getMessager().printMessage(ERROR, message, setting);
                    }
                });

        var injectSymbols = environment.getElementsAnnotatedWith(Inject.class);
        var providerSymbols = environment.getElementsAnnotatedWith(Provider.class);

        var classes = Stream.of(settingsSymbols, injectSymbols.stream(), providerSymbols.stream())
                .reduce(Stream::concat)
                .orElse(Stream.empty())
                .map(Element::getEnclosingElement)
                .filter(this::isExtension)
                .collect(Collectors.toSet());

        classes.addAll(environment.getElementsAnnotatedWith(Requires.class));
        classes.addAll(environment.getElementsAnnotatedWith(Provides.class));
        classes.addAll(environment.getElementsAnnotatedWith(Extension.class));

        return classes;
    }

    /**
     * Checks whether an element has "ServiceExtension" in their type hierarchy
     */
    private boolean isExtension(Element element) {

        var t = (TypeElement) element;
        while (t != null && !t.toString().equals(Object.class.getName())) {
            if (t.getInterfaces().stream().anyMatch(p -> p.toString().contains(SERVICE_EXTENSION_NAME))) {
                return true;
            }
            t = (TypeElement) typeUtils.asElement(t.getSuperclass());
            // do whatever with element
        }
        return false;
    }
}
