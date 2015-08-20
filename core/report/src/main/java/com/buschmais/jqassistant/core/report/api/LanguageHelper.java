package com.buschmais.jqassistant.core.report.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.spi.reflection.AnnotatedType;

/**
 * Provides utility functionality for creating reports.
 */
public final class LanguageHelper {

    /**
     * Return the {@link LanguageElement} associated with a {@link com.buschmais.jqassistant.core.store.api.model.Descriptor}.
     *
     * The method uses a breadth-first-search to identify a descriptor type annotated with {@link LanguageElement}.
     * 
     * @param descriptor
     *            The descriptor.
     * @return The resolved {@link LanguageElement}
     */
    public static LanguageElement getLanguageElement(Descriptor descriptor) {
        Queue<Class<?>> queue = new LinkedList<>();
        Class<?>[] descriptorTypes = descriptor.getClass().getInterfaces();
        do {
            queue.addAll(Arrays.asList(descriptorTypes));
            Class<?> descriptorType = queue.poll();
            AnnotatedType annotatedType = new AnnotatedType(descriptorType);
            Annotation languageAnnotation = annotatedType.getByMetaAnnotation(Language.class);
            if (languageAnnotation != null) {
                return getAnnotationValue(languageAnnotation, "value", LanguageElement.class);
            }
            descriptorTypes = descriptorType.getInterfaces();
        }
        while (!queue.isEmpty());
        return null;
    }

    /**
     * Return a value from an annotation.
     * 
     * @param annotation
     *            The annotation.
     * @param value
     *            The value.
     * @param expectedType
     *            The expected type.
     * @param <T>
     *            The expected type.
     * @return The value.
     */
    private static <T> T getAnnotationValue(Annotation annotation, String value, Class<T> expectedType) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Method valueMethod;
        try {
            valueMethod = annotationType.getDeclaredMethod(value);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot resolve required method '" + value + "()' for '" + annotationType + "'.");
        }
        Object elementValue;
        try {
            elementValue = valueMethod.invoke(annotation);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot invoke method value() for " + annotationType);
        }
        return elementValue != null ? expectedType.cast(elementValue) : null;
    }

}
