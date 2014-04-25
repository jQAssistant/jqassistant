package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.Field;

import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor.Declares;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Describes a field (i.e. static or instance variable) of a Java class.
 */
@Java(Field)
@Label(value = "FIELD")
public interface FieldDescriptor extends SignatureDescriptor, NamedDescriptor, TypedDescriptor, DependentDescriptor, AnnotatedDescriptor,
        AccessModifierDescriptor {

    @Incoming
    @Declares
    TypeDescriptor getDeclaringType();

    /**
     * @return the transientField
     */
    @Property("TRANSIENT")
    Boolean isTransient();

    /**
     * @param transientField
     *            the transientField to set
     */
    void setTransient(Boolean transientField);

    /**
     * @return the volatileField
     */
    @Property("VOLATILE")
    Boolean isVolatile();

    /**
     * @param volatileField
     *            the volatileField to set
     */
    void setVolatile(Boolean volatileField);

}
