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

package org.eclipse.edc.plugins.autodoc.core.processor.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor9;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

/**
 * Contains convenience methods for working with Java Compiler API annotation mirrors.
 * <p>
 * These methods delegate to {@link SimpleAnnotationValueVisitor9} implementations to walk trees produced by the compiler and resolve requested values.
 */
public class AnnotationFunctions {
    private static final AnnotationClassValuesResolver CLASS_VALUES_RESOLVER = new AnnotationClassValuesResolver();
    private static final AnnotationStringValuesResolver STRING_VALUES_RESOLVER = new AnnotationStringValuesResolver();
    private static final AnnotationBooleanValueResolver BOOLEAN_VALUE_RESOLVER = new AnnotationBooleanValueResolver();
    private static final AnnotationStringValueResolver STRING_VALUE_RESOLVER = new AnnotationStringValueResolver();
    private static final AnnotationLongValueResolver LONG_VALUE_RESOLVER = new AnnotationLongValueResolver();
    private static final String ANNOTATION_METHOD_POSTFIX = "()";

    private AnnotationFunctions() {
    }

    /**
     * Returns the annotation mirror for the annotated element corresponding to the given annotation type, or null if no type is found.
     */
    @Nullable
    public static AnnotationMirror mirrorFor(Class<? extends Annotation> type, Element element) {
        var elementAnnotations = element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : elementAnnotations) {
            if (type.getName().equals(annotationMirror.getAnnotationType().toString())) {
                return annotationMirror;
            }
        }
        return null;
    }

    /**
     * Returns the type mirror for a method's return type. Assumes that {@code element} refers to an executable type, most likely a {@code Type$MethodType}
     */
    public static TypeMirror mirrorForReturn(Element element) {
        var type = element.asType();
        if (type instanceof ExecutableType) { //method
            var methodSymbol = (ExecutableType) type;
            return methodSymbol.getReturnType();
        }
        return null;
    }

    /**
     * Returns the annotation attribute value of the given type and name.
     *
     * @param type             the value type
     * @param name             the attribute name
     * @param annotationMirror the annotation
     * @param elementUtils     the current elements instance
     */
    public static <T> T attributeValue(Class<T> type, String name, AnnotationMirror annotationMirror, Elements elementUtils) {
        name += ANNOTATION_METHOD_POSTFIX;
        var resolver = getResolver(type);
        for (var entry : elementUtils.getElementValuesWithDefaults(annotationMirror).entrySet()) {
            if (name.equals(entry.getKey().toString())) {
                return type.cast(entry.getValue().accept(resolver, null));
            }
        }
        throw new IllegalArgumentException(format("Unknown attribute '%s' for annotation: %s", name, annotationMirror.getAnnotationType().asElement()));
    }

    /**
     * Returns the annotation attribute value as a collection of strings.
     *
     * @param name             the attribute name
     * @param annotationMirror the annotation
     * @param elementUtils     the current elements instance
     */
    public static List<String> attributeStringValues(String name, AnnotationMirror annotationMirror, Elements elementUtils) {
        name += ANNOTATION_METHOD_POSTFIX;
        for (var entry : elementUtils.getElementValuesWithDefaults(annotationMirror).entrySet()) {
            if (name.equals(entry.getKey().toString())) {
                return entry.getValue().accept(STRING_VALUES_RESOLVER, new ArrayList<>());
            }
        }
        throw new IllegalArgumentException(format("Unknown attribute '%s' for annotation: %s", name, annotationMirror.getAnnotationType().asElement()));
    }

    /**
     * Returns the annotation attribute value as a collection of type names.
     *
     * @param name             the attribute name
     * @param annotationMirror the annotation
     * @param elementUtils     the current elements instance
     */
    public static List<String> attributeTypeValues(String name, AnnotationMirror annotationMirror, Elements elementUtils) {
        name += ANNOTATION_METHOD_POSTFIX;
        for (var entry : elementUtils.getElementValuesWithDefaults(annotationMirror).entrySet()) {
            if (name.equals(entry.getKey().toString())) {
                return entry.getValue().accept(CLASS_VALUES_RESOLVER, new ArrayList<>());
            }
        }
        return emptyList();
    }

    @NotNull
    private static <T> SimpleAnnotationValueVisitor9<?, ?> getResolver(Class<T> type) {
        SimpleAnnotationValueVisitor9<?, ?> resolver;
        if (Boolean.class.equals(type)) {
            resolver = BOOLEAN_VALUE_RESOLVER;
        } else if (String.class.equals(type)) {
            resolver = STRING_VALUE_RESOLVER;
        } else if (Long.class.equals(type)) {
            resolver = LONG_VALUE_RESOLVER;
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + type.getName());
        }
        return resolver;
    }
}
