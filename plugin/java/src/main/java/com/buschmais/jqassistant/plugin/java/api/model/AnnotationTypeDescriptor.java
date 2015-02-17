package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.plugin.java.api.report.Java;

/**
 * Denotes an annotation type.
 */
@Java(Java.JavaLanguageElement.Type)
public interface AnnotationTypeDescriptor extends ClassFileDescriptor, AnnotationDescriptor {
}
