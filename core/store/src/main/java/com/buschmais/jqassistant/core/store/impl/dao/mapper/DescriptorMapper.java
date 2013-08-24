package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ValueDescriptor;
import com.buschmais.jqassistant.core.store.api.model.NodeLabel;
import com.buschmais.jqassistant.core.store.api.model.NodeProperty;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import org.neo4j.graphdb.Label;

import java.util.Map;
import java.util.Set;

/**
 * Defines an interface to map an {@link Descriptor} to nodes and
 * relationships.
 *
 * @param <T> The descriptor types.
 */
public interface DescriptorMapper<T extends Descriptor> {

    /**
     * Return the java types.
     *
     * @return The java types.
     */
    public Set<Class<? extends T>> getJavaType();

    /**
     * Return the {@link NodeLabel}s.
     *
     * @return The {@link NodeLabel}s.
     */
    public NodeLabel getCoreLabel();

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
     *
     * @param labels The labels provided by a node.
     * @return The java type.
     */
    public Class<? extends T> getType(Set<Label> labels);


    /**
     * Return a {@link Map} containing all outgoing relations with the
     * {@link com.buschmais.jqassistant.core.store.api.model.Relation} as key and the target descriptors as value for the
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
