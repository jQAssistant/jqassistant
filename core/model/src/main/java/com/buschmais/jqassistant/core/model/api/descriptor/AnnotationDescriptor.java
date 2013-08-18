package com.buschmais.jqassistant.core.model.api.descriptor;

import java.util.Set;

/**
 * Describes an annotation.
 */
public class AnnotationDescriptor extends AbstractDescriptor {

    private TypeDescriptor implementsType;

    private Set<PrimitiveValueDescriptor> values;
}
