package com.buschmais.jqassistant.store.impl.dao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.index.Index;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.store.api.DescriptorDAO;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.QueryResult;
import com.buschmais.jqassistant.store.impl.dao.mapper.DescriptorMapper;
import com.buschmais.jqassistant.store.impl.model.RelationType;

public class DescriptorDAOImpl implements DescriptorDAO {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DescriptorDAOImpl.class);

	private final GraphDatabaseService database;
	private final DescriptorAdapterRegistry registry;
	private final ExecutionEngine executionEngine;

	private final Map<String, AbstractDescriptor> indexCache = new HashMap<String, AbstractDescriptor>();

	private final Set<AbstractDescriptor> newDescriptors = new HashSet<AbstractDescriptor>();

	private final DescriptorCache descriptorCache = new DescriptorCache();

	public DescriptorDAOImpl(DescriptorAdapterRegistry registry,
			GraphDatabaseService database) {
		this.registry = registry;
		this.database = database;
		this.executionEngine = new ExecutionEngine(database);
	}

	@Override
	public <T extends AbstractDescriptor> void persist(T descriptor) {
		Node node;
		LOGGER.debug("Creating node for '{}'.",
				descriptor.getFullQualifiedName());
		DescriptorMapper<T> adapter = registry.getDescriptorAdapter(descriptor
				.getClass());
		node = database.createNode();
		adapter.setId(descriptor, Long.valueOf(node.getId()));
		node.setProperty(NodeProperty.TYPE.name(), adapter.getNodeType().name());
		node.setProperty(NodeProperty.FQN.name(),
				descriptor.getFullQualifiedName());
		Index<Node> index = adapter.getIndex();
		if (index != null) {
			index.add(node, NodeProperty.FQN.name(),
					descriptor.getFullQualifiedName());
			indexCache.put(descriptor.getFullQualifiedName(), descriptor);
		}
		descriptorCache.put(descriptor, node);
		newDescriptors.add(descriptor);
	}

	@Override
	public void flush() {
		for (AbstractDescriptor descriptor : newDescriptors) {
			flushRelations(descriptor);
		}
		newDescriptors.clear();
		indexCache.clear();
	}

	@Override
	public <T extends AbstractDescriptor> T find(Class<T> type,
			String fullQualifiedName) {
		@SuppressWarnings("unchecked")
		T descriptor = (T) indexCache.get(fullQualifiedName);
		if (descriptor == null) {
			DescriptorMapper<AbstractDescriptor> adapter = registry
					.getDescriptorAdapter(type);
			Index<Node> index = adapter.getIndex();
			if (index != null) {
				Node node = index.get(NodeProperty.FQN.name(),
						fullQualifiedName).getSingle();
				if (node != null) {
					descriptor = createFrom(type, node);
					indexCache.put(fullQualifiedName, descriptor);
					return descriptor;
				}
			}
		}
		return null;
	}

	@Override
	public QueryResult executeQuery(String query, Map<String, Object> parameters) {
		ExecutionResult result = executionEngine.execute(query, parameters);
		final Iterator<Map<String, Object>> iterator = result.iterator();
		Iterable<Map<String, Object>> rowIterable = new Iterable<Map<String, Object>>() {

			@Override
			public Iterator<Map<String, Object>> iterator() {
				return new Iterator<Map<String, Object>>() {

					@Override
					public boolean hasNext() {
						return iterator.hasNext();
					}

					@Override
					public Map<String, Object> next() {
						Map<String, Object> row = new HashMap<String, Object>();
						for (Entry<String, Object> entry : iterator.next()
								.entrySet()) {
							String name = entry.getKey();
							Object value = entry.getValue();
							Object decodedValue = decodeValue(value);
							row.put(name, decodedValue);
						}
						return row;
					}

					@Override
					public void remove() {
						iterator.remove();
					}

					private Object decodeValue(Object value) {
						Object decodedValue;
						if (value instanceof Node) {
							Node node = (Node) value;
							return createFrom(node);
						} else {
							decodedValue = value;
						}
						return decodedValue;
					}

				};
			}
		};
		return new QueryResult(result.columns(), rowIterable);
	}

	/**
	 * Flushes the relations of the given descriptor to the {@link Node} it
	 * represents.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 */
	private <T extends AbstractDescriptor> void flushRelations(T descriptor) {
		Node node = findNode(descriptor);
		DescriptorMapper<T> adapter = registry.getDescriptorAdapter(descriptor
				.getClass());
		Map<RelationType, Set<? extends AbstractDescriptor>> relations = adapter
				.getRelations(descriptor);
		for (Entry<RelationType, Set<? extends AbstractDescriptor>> relationEntry : relations
				.entrySet()) {
			Set<? extends AbstractDescriptor> targetDescriptors = relationEntry
					.getValue();
			for (AbstractDescriptor targetDescriptor : targetDescriptors) {
				Node targetNode = findNode(targetDescriptor);
				RelationType relationType = relationEntry.getKey();
				node.createRelationshipTo(targetNode, relationType);
			}
		}
	}

	/**
	 * Find the {@link Node} which represents the given descriptor.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @return The {@link Node}.
	 */
	private <T extends AbstractDescriptor> Node findNode(T descriptor) {
		DescriptorMapper<T> adapter = registry.getDescriptorAdapter(descriptor
				.getClass());
		Long id = adapter.getId(descriptor);
		Node node = this.descriptorCache.findBy(id);
		if (node == null) {
			Index<Node> index = adapter.getIndex();
			if (index != null) {
				node = index.get(NodeProperty.FQN.name(),
						descriptor.getFullQualifiedName()).getSingle();
				if (node != null) {
					descriptorCache.put(descriptor, node);
				}
			}
		}
		return node;
	}

	/**
	 * Creates a descriptor instance from the given {@link Node}.
	 * <p>
	 * A new descriptor instance is created if no matching one can be found in
	 * the {@link #descriptorCache}.
	 * </p>
	 * 
	 * @param node
	 *            The {@link Node}.
	 * @return The descriptor.
	 */
	@SuppressWarnings("unchecked")
	private <T extends AbstractDescriptor> T createFrom(Node node) {
		return (T) createFrom(
				registry.getDescriptorAdapter(node).getJavaType(), node);
	}

	/**
	 * Creates a descriptor instance from the given {@link Node}.
	 * <p>
	 * A new descriptor instance is created if no matching one can be found in
	 * the {@link #descriptorCache}.
	 * </p>
	 * 
	 * @param type
	 *            The class type of the descriptor.
	 * @param node
	 *            The {@link Node}.
	 * @return The descriptor.
	 */
	private <T extends AbstractDescriptor> T createFrom(Class<T> type, Node node) {
		T descriptor = descriptorCache.findBy(node);
		if (descriptor == null) {
			// find adapter and create instance
			DescriptorMapper<T> adapter = registry.getDescriptorAdapter(type);
			descriptor = adapter.createInstance();
			adapter.setId(descriptor, Long.valueOf(node.getId()));
			descriptorCache.put(descriptor, node);
			descriptor.setFullQualifiedName((String) node
					.getProperty(NodeProperty.FQN.name()));
			// create outgoing relationships
			Map<RelationType, Set<AbstractDescriptor>> relations = new HashMap<RelationType, Set<AbstractDescriptor>>();
			for (Relationship relationship : node
					.getRelationships(Direction.OUTGOING)) {
				Node targetNode = relationship.getEndNode();
				AbstractDescriptor targetDescriptor = createFrom(targetNode);
				RelationType relationType = RelationType.valueOf(relationship
						.getType().name());
				Set<AbstractDescriptor> set = relations.get(relationType);
				if (set == null) {
					set = new HashSet<AbstractDescriptor>();
					relations.put(relationType, set);
				}
				set.add(targetDescriptor);
			}
			adapter.setRelations(descriptor, relations);
		}
		return descriptor;
	}
}
