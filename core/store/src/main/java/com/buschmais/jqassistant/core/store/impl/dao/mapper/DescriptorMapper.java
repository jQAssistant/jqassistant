package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

import java.util.Map;
import java.util.Set;

/**
 * Defines an interface to map an {@link Descriptor} to nodes and
 * relationships.
 *
 * @param <T> The descriptor type.
 */
public interface DescriptorMapper<T extends Descriptor> {

    /**
     * Return the java types.
     *
     * @return The java types.
     */
    public Set<Class<? extends T>> getJavaType();

    /**
     * Return the {@link com.buschmais.jqassistant.core.store.api.model.IndexedLabel}.
     *
     * @return The {@link com.buschmais.jqassistant.core.store.api.model.IndexedLabel}.
     */
    public IndexedLabel getPrimaryLabel();

    /**
     * Return the names of all properties.
     *
     * @return The names of all properties.
     */
    public Iterable<String> getPropertyNames();

    /**
     * Return the names of all relations.
     *
     * @return The names of all relations.
     */
    public Iterable<? extends RelationshipType> getRelationshipTypes();

    /**
     * Creates a descriptor instance.
     *
     * @param type The java type.
     * @return The descriptor instance.
     */
    public T createInstance(Class<? extends T> type);

    /**
     * Return the java type.
     *
     * @param labels The labels provided by a node.
     * @return The java type.
     */
    public Class<? extends T> getType(Set<Label> labels);

    /**
     * Return a relation.
     *
     * @param descriptor       The descriptor.
     * @param relationshipType The relationshipType.
     * @return The target descriptors.
     */
    public Set<? extends Descriptor> getRelation(T descriptor, RelationshipType relationshipType);

    /**
     * Set the outgoing relations for the given descriptor.
     *
     * @param descriptor       The descriptor.
     * @param relationshipType The relationshipType.
     * @param targets          The target descriptors.
     */
    public void setRelation(T descriptor, RelationshipType relationshipType, Set<? extends Descriptor> targets);

    /**
     * Return the id of the descriptor.
     *
     * @param descriptor The descriptor.
     * @return The id.
     */
    public Long getId(T descriptor);

    /**
     * Set the id of the descriptor.
     *
     * @param descriptor The descriptor.
     * @param id         The id.
     */
    public void setId(T descriptor, Long id);

    /**
     * Returns a {@link Map} with all properties of this descriptor.
     *
     * @param descriptor   The properties of this descriptor will be returned.
     * @param propertyName The property name.
     * @return a {@link Map} with all properties.
     */
    public Object getProperty(T descriptor, String propertyName);

    /**
     * Set the property on the passed descriptor.
     *
     * @param descriptor   the target descriptor
     * @param propertyName the property name
     * @param value        the value of the property
     */
    public void setProperty(T descriptor, String propertyName, Object value);

    /**
     * Return the set of labels to be set on the node.
     *
     *
     * @param descriptor The descriptor.
     * @return The set of labels.
     */
    public Set<? extends Label> getLabels(T descriptor);

    /**
     * Set a label on the descriptor.
     *
     * @param descriptor The descriptor.
     * @param label      The label.
     */
    public void setLabel(T descriptor, Label label);
}
