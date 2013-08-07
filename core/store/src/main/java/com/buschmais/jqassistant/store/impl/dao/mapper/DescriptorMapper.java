package com.buschmais.jqassistant.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.NodeProperty;
import com.buschmais.jqassistant.store.api.model.Relation;
import org.neo4j.graphdb.Label;

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
     * Return the java type.
     *
     * @return The java type.
     */
    public Class<T> getJavaType();

    /**
     * Return the {@link NodeLabel }.
     *
     * @return The {@link NodeLabel}.
     */
    public NodeLabel getCoreLabel();

    /**
     * Creates a descriptor instance.
     *
     * @return The descriptor instance.
     */
    public T createInstance();

    /**
     * Return a {@link Map} containing all outgoing relations with the
     * {@link com.buschmais.jqassistant.store.api.model.Relation} as key and the target descriptors as value for the
     * given descriptor.
     *
     * @param descriptor The descriptor.
     * @return The relations {@link Map}.
     */
    public Map<Relation, Set<? extends Descriptor>> getRelations(T descriptor);

    /**
     * Set the outgoing relations for the given descriptor.
     *
     * @param descriptor The descriptor.
     * @param relations  The relations map.
     */
    public void setRelations(T descriptor, Map<Relation, Set<Descriptor>> relations);

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
     * @param descriptor The properties of this descriptor will be returned.
     * @return a {@link Map} with all properties.
     */
    public Map<NodeProperty, Object> getProperties(T descriptor);

    /**
     * Set the property on the passed descriptor.
     *
     * @param descriptor the target descriptor
     * @param property   the property name
     * @param value      the value of the property
     */
    public void setProperty(T descriptor, NodeProperty property, Object value);

    /**
     * Return the set of labels to be set on the node.
     *
     * @param descriptor The descriptor.
     * @return The set of labels.
     */
    public Set<Label> getLabels(T descriptor);

    /**
     * Set a label on the descriptor.
     *
     * @param descriptor The descriptor.
     * @param label      The label.
     */
    public void setLabel(T descriptor, Label label);
}
