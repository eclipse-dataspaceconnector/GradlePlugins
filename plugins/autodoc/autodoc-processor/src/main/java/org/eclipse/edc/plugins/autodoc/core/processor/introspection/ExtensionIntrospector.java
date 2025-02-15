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

import org.eclipse.edc.plugins.autodoc.core.processor.compiler.AnnotationFunctions;
import org.eclipse.edc.runtime.metamodel.annotation.Configuration;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.runtime.metamodel.annotation.Provider;
import org.eclipse.edc.runtime.metamodel.annotation.Provides;
import org.eclipse.edc.runtime.metamodel.annotation.Setting;
import org.eclipse.edc.runtime.metamodel.annotation.SettingContext;
import org.eclipse.edc.runtime.metamodel.annotation.Spi;
import org.eclipse.edc.runtime.metamodel.domain.ConfigurationSetting;
import org.eclipse.edc.runtime.metamodel.domain.Service;
import org.eclipse.edc.runtime.metamodel.domain.ServiceReference;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.eclipse.edc.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeStringValues;
import static org.eclipse.edc.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeTypeValues;
import static org.eclipse.edc.plugins.autodoc.core.processor.compiler.AnnotationFunctions.attributeValue;
import static org.eclipse.edc.plugins.autodoc.core.processor.compiler.AnnotationFunctions.mirrorFor;
import static org.eclipse.edc.plugins.autodoc.core.processor.compiler.ElementFunctions.typeFor;

/**
 * Contains methods for introspecting any given extension (represented by an {@link Element}) using the Java Compiler API.
 */
public class ExtensionIntrospector {
    public static final String CONTEXT_ATTRIBUTE = "context";
    private final Elements elementUtils;
    private final Types typeUtils;

    public ExtensionIntrospector(Elements elementUtils, Types typeUtils) {
        this.elementUtils = elementUtils;
        this.typeUtils = typeUtils;
    }

    /**
     * Returns module categories set using either the {@link Spi} or {@link Extension} annotation.
     */
    public List<String> getExtensionCategories(Element extensionElement) {
        var annotationMirror = mirrorFor(Extension.class, extensionElement);
        return annotationMirror != null ? attributeStringValues("categories", annotationMirror, elementUtils) : Collections.emptyList();
    }

    /**
     * Resolves referenced services by introspecting usages of {@link Inject}.
     */
    public List<ServiceReference> resolveReferencedServices(Element extensionElement) {
        return getEnclosedElementsAnnotatedWith(extensionElement, Inject.class)
                .map(element -> {
                    var required = attributeValue(Boolean.class, "required", mirrorFor(Inject.class, element), elementUtils);
                    return new ServiceReference(typeFor(element), required);
                })
                .collect(toList());
    }

    /**
     * Resolves referenced services by introspecting the {@link Provides} annotation.
     */
    public List<Service> resolveProvidedServices(Element element) {

        // class annotation @Provides
        var providesServices = ofNullable(mirrorFor(Provides.class, element))
                .map(mirror -> attributeTypeValues("value", mirror, elementUtils).stream())
                .orElse(Stream.empty());

        // @Provider methods
        var providerMethodServices = getEnclosedElementsAnnotatedWith(element, Provider.class)
                .map(AnnotationFunctions::mirrorForReturn)
                .filter(Objects::nonNull)
                .map(TypeMirror::toString);

        return Stream.concat(providesServices, providerMethodServices)
                .distinct()
                .map(Service::new)
                .collect(toList());
    }

    /**
     * Resolves configuration points declared with {@link Setting}.
     */
    public List<ConfigurationSetting> resolveConfigurationSettings(Element element) {
        var settingsInConfigObjects = getEnclosedElementsAnnotatedWith(element, Configuration.class)
                .flatMap(e -> getEnclosedElementsAnnotatedWith(typeUtils.asElement(e.asType()), Setting.class));

        return Stream.concat(settingsInConfigObjects, getEnclosedElementsAnnotatedWith(element, Setting.class))
                .filter(VariableElement.class::isInstance)
                .map(VariableElement.class::cast)
                .map(this::createConfigurationSetting)
                .collect(toList());
    }

    public String getExtensionName(Element extensionElement) {
        var annotationMirror = mirrorFor(Extension.class, extensionElement);
        return annotationMirror != null ?
                attributeValue(String.class, "value", annotationMirror, elementUtils) :
                extensionElement.getSimpleName().toString();
    }

    public String getExtensionClassname(Element element) {
        return element.asType().toString();
    }


    /**
     * Returns a stream consisting of the {@code extensionElement}'s enclosed {@link Element}s, that are annotated with the given annotation class.
     */
    private Stream<? extends Element> getEnclosedElementsAnnotatedWith(Element extensionElement, Class<? extends Annotation> annotationClass) {
        return extensionElement.getEnclosedElements()
                .stream().filter(e -> e.getAnnotation(annotationClass) != null);
    }

    /**
     * Maps a {@link ConfigurationSetting} from an {@link Setting} annotation.
     */
    private ConfigurationSetting createConfigurationSetting(VariableElement settingElement) {
        var settingMirror = mirrorFor(Setting.class, settingElement);
        var prefix = attributeValue(String.class, CONTEXT_ATTRIBUTE, settingMirror, elementUtils);
        if (prefix.isEmpty()) {
            prefix = resolveConfigurationPrefix(settingElement);
        }

        // either take the config key value directly from the annotated variable or from the "key" attribute
        var keyValue = prefix + ofNullable(settingElement.getConstantValue())
                .orElseGet(() -> attributeValue(String.class, "key", settingMirror, elementUtils))
                .toString();

        var description = Stream.of(
                attributeValue(String.class, "description", settingMirror, elementUtils),
                attributeValue(String.class, "value", settingMirror, elementUtils)
        ).filter(Objects::nonNull).filter(it -> !it.isEmpty()).findFirst().orElse(null);

        return ConfigurationSetting.Builder.newInstance()
                .key(keyValue)
                .description(description)
                .type(attributeValue(String.class, "type", settingMirror, elementUtils))
                .required(attributeValue(Boolean.class, "required", settingMirror, elementUtils))
                .maximum(attributeValue(Long.class, "max", settingMirror, elementUtils))
                .minimum(attributeValue(Long.class, "min", settingMirror, elementUtils))
                .defaultValue(attributeValue(String.class, "defaultValue", settingMirror, elementUtils))
                .deprecated(mirrorFor(Deprecated.class, settingElement) != null)
                .build();
    }

    /**
     * Resolves a configuration prefix specified by {@link SettingContext} for a given EDC setting element or an empty string if there is none.
     */
    @NotNull
    private String resolveConfigurationPrefix(VariableElement edcSettingElement) {
        var enclosingElement = edcSettingElement.getEnclosingElement();
        if (enclosingElement == null) {
            return "";
        }
        var contextMirror = mirrorFor(SettingContext.class, enclosingElement);
        return contextMirror != null ? attributeValue(String.class, "value", contextMirror, elementUtils) : "";
    }
}
