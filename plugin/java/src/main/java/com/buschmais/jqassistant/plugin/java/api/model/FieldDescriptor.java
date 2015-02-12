package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Describes a field (i.e. static or instance variable) of a Java class.
 */
@Java(Java.JavaLanguageElement.Field)
@Label(value = "Field")
public interface FieldDescriptor extends MemberDescriptor, NamedDescriptor, TypedDescriptor, AnnotatedDescriptor, AccessModifierDescriptor {

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

    List<WritesDescriptor> getWrittenBy();

    List<ReadsDescriptor> getReadBy();
}
