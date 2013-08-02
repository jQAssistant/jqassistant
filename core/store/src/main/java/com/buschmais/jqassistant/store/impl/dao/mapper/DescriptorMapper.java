package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.api.model.NodeProperty;
import com.buschmais.jqassistant.store.api.model.Relation;

/**
 * Defines an interface to map an {@link AbstractDescriptor} to nodes and
 * relationships.
 *
 * @param <T> The descriptor type.
 */
public interface DescriptorMapper<T extends AbstractDescriptor> {

    /**
     * Return the java type.
     *
     * @return The java type.
     */
    public Class<T> getJavaType();

    /**
     * Return the {@link com.buschmais.jqassistant.store.api.model.graph.NodeLabel}.
     *
     * @return The {@link com.buschmais.jqassistant.store.api.model.graph.NodeLabel}.
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
    public Map<Relation, Set<? extends AbstractDescriptor>> getRelations(T descriptor);

    /**
     * Set the outgoing relations for the given descriptor.
     *
     * @param descriptor The descriptor.
     * @param relations  The relations map.
     * @param target     The target descriptor.
     */
    public void setRelations(T descriptor, Map<Relation, Set<AbstractDescriptor>> relations);

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
	 * @param descriptor
	 *            The properties of this descriptor will be returned.
	 * @return a {@link Map} with all properties.
	 */
	public Map<NodeProperty, Object> getProperties(T descriptor);

	/**
	 * Set the property on the passed descriptor.
	 *
	 * @param descriptor
	 *            the target descriptor
	 * @param property
	 *            the property name
	 * @param value
	 *            the value of the property
	 */
	public void setProperty(T descriptor, NodeProperty property, Object value);
}
