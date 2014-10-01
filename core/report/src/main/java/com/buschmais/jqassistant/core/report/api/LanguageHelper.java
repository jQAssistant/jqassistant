package com.buschmais.jqassistant.core.report.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.store.api.type.Descriptor;
import com.buschmais.xo.spi.reflection.AnnotatedType;

/**
 * Provides utility functionality for creating reports.
 */
public final class LanguageHelper {

    /**
     * Return the {@link LanguageElement} associated with a
     * {@link com.buschmais.jqassistant.core.store.api.type.Descriptor}.
     *
     * @param descriptor
     *            The descriptor.
     * @return The resolved {@link LanguageElement}
     *
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     */
    public static LanguageElement getLanguageElement(Descriptor descriptor) throws AnalysisListenerException {
        for (Class<?> descriptorType : descriptor.getClass().getInterfaces()) {
            AnnotatedType annotatedType = new AnnotatedType(descriptorType);
            Annotation languageAnnotation = annotatedType.getByMetaAnnotation(Language.class);
            if (languageAnnotation != null) {
                return getAnnotationValue(languageAnnotation, "value", LanguageElement.class);
            }
        }
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
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the value cannot be determined from the annotation.
     */
    private static <T> T getAnnotationValue(Annotation annotation, String value, Class<T> expectedType) throws AnalysisListenerException {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Method valueMethod;
        try {
            valueMethod = annotationType.getDeclaredMethod(value);
        } catch (NoSuchMethodException e) {
            throw new AnalysisListenerException("Cannot resolve required method '" + value + "()' for '" + annotationType + "'.");
        }
        Object elementValue;
        try {
            elementValue = valueMethod.invoke(annotation);
        } catch (ReflectiveOperationException e) {
            throw new AnalysisListenerException("Cannot invoke method value() for " + annotationType);
        }
        return elementValue != null ? expectedType.cast(elementValue) : null;
    }

}
