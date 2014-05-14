package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.Field;

import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Describes a field (i.e. static or instance variable) of a Java class.
 */
@Java(Field)
@Label(value = "Field")
public interface FieldDescriptor extends MemberDescriptor, NamedDescriptor, TypedDescriptor, DependentDescriptor, AnnotatedDescriptor,
        AccessModifierDescriptor {

    /**
     * @return the transientField
     */
    @Property("transient")
    Boolean isTransient();

    /**
     * @param transientField
     *            the transientField to set
     */
    void setTransient(Boolean transientField);

    /**
     * @return the volatileField
     */
    @Property("volatile")
    Boolean isVolatile();

    /**
     * @param volatileField
     *            the volatileField to set
     */
    void setVolatile(Boolean volatileField);

}
