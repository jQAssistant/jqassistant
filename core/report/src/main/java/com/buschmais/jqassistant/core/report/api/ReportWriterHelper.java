package com.buschmais.jqassistant.core.report.api;

import com.buschmais.jqassistant.core.report.api.Language;
import com.buschmais.jqassistant.core.report.api.LanguageElement;
import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.xo.spi.reflection.AnnotatedType;

import javax.xml.stream.XMLStreamException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by Dirk Mahler on 28.04.2014.
 */
public final class ReportWriterHelper {

    private ReportWriterHelper(){
    }

    public static LanguageElement getLanguageElement(Descriptor descriptor) throws XMLStreamException {
        for (Class<?> descriptorType : descriptor.getClass().getInterfaces()) {
            AnnotatedType annotatedType = new AnnotatedType(descriptorType);
            Annotation languageAnnotation = annotatedType.getByMetaAnnotation(Language.class);
            if (languageAnnotation != null) {
                Class<? extends Annotation> annotationType = languageAnnotation.annotationType();
                return getAnnotationValue(languageAnnotation, "value", LanguageElement.class);
            }
        }
        return null;
    }

    private static <T> T getAnnotationValue(Annotation annotation, String value, Class<T> expectedType) throws XMLStreamException {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        Method valueMethod;
        try {
            valueMethod = annotationType.getDeclaredMethod(value);
        } catch (NoSuchMethodException e) {
            throw new XMLStreamException("Cannot resolve required method '" + value + "()' for '" + annotationType + "'.");
        }
        Object elementValue;
        try {
            elementValue = valueMethod.invoke(annotation);
        } catch (ReflectiveOperationException e) {
            throw new XMLStreamException("Cannot invoke method value() for " + annotationType);
        }
        return elementValue != null ? expectedType.cast(elementValue) : null;
    }
}