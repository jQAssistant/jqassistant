package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.impl.model.NodeType;
import com.buschmais.jqassistant.store.impl.model.RelationType;

/**
 * Defines an interface to map an {@link AbstractDescriptor} to nodes and
 * relationships.
 * 
 * @param <T>
 *            The descriptor type.
 */
public interface DescriptorMapper<T extends AbstractDescriptor> {

	/**
	 * Return the java type.
	 * 
	 * @return The java type.
	 */
	public Class<T> getJavaType();

	/**
	 * Return the {@link NodeType}.
	 * 
	 * @return The {@link NodeType}.
	 */
	public NodeType getNodeType();

	/**
	 * Get the {@link Node} {@link Index} to lookup instances.
	 * 
	 * @return {@link Node} {@link Index} .
	 */
	public Index<Node> getIndex();

	/**
	 * Creates a descriptor instance.
	 * 
	 * @return The descriptor instance.
	 */
	public T createInstance();

	/**
	 * Return a {@link Map} containing all outgoing relations with the
	 * {@link RelationType} as key and the target descriptors as value for the
	 * given descriptor.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @return The {@link Map}.
	 */
	public Map<RelationType, Set<? extends AbstractDescriptor>> getRelations(
			T descriptor);

	/**
	 * Set the outgoing relation for the given descriptor, {@link RelationType}
	 * and target descriptor.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @param relation
	 *            The {@link RelationType}.
	 * @param target
	 *            The target descriptor.
	 */
	public void setRelation(T descriptor, RelationType relation,
			AbstractDescriptor target);

	/**
	 * Return the id of the descriptor.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @return The id.
	 */
	public Long getId(T descriptor);

	/**
	 * Set the id of the descriptor.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @param id
	 *            The id.
	 */
	public void setId(T descriptor, Long id);
}
