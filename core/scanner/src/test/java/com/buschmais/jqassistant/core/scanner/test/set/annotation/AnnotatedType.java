package com.buschmais.jqassistant.core.scanner.test.set.annotation;

import static com.buschmais.jqassistant.core.scanner.test.set.annotation.Enumeration.NON_DEFAULT;

/**
 * An annotated type.
 */
@Annotation(value = "class", arrayValue = {"a", "b"}, classValue = Number.class, enumerationValue = NON_DEFAULT, nestedAnnotationValue = @NestedAnnotation("nestedClass"))
public class AnnotatedType {

    @Annotation(value = "field")
    private String annotatedField;

    @Annotation("method")
    public void annotatedMethod(@Annotation("parameter") String parameter) {
        @Annotation("localField") String annotatedLocalField;
    }

}
