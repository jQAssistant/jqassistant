package com.buschmais.jqassistant.core.store.impl.dao;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.store.api.DescriptorDAO;
import com.buschmais.jqassistant.core.store.api.QueryResult;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.DescriptorMapper;

public class DescriptorDAOImpl implements DescriptorDAO {

	private final class RowIterable implements Iterable<QueryResult.Row>, Closeable {

		private List<String> columns;
		private ResourceIterator<Map<String, Object>> iterator;

		private RowIterable(List<String> columns, ResourceIterator<Map<String, Object>> iterator) {
			this.columns = columns;
			this.iterator = iterator;
		}

		@Override
		public Iterator<QueryResult.Row> iterator() {

			return new Iterator<QueryResult.Row>() {

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public QueryResult.Row next() {
					Map<String, Object> next = iterator.next();
					Map<String, Object> row = new LinkedHashMap<>();
					for (String column : columns) {
						Object value = next.get(column);
						Object decodedValue = decodeValue(value);
						row.put(column, decodedValue);
					}
					return new QueryResult.Row(row);
				}

				@Override
				public void remove() {
					iterator.remove();
				}

				private Object decodeValue(Object value) {
					Object decodedValue;
					if (value instanceof Node) {
						Node node = (Node) value;
						return getDescriptor(node);
					} else if (value instanceof List<?>) {
						List<?> listValue = (List<?>) value;
						List<Object> decodedList = new ArrayList<>();
						for (Object o : listValue) {
							decodedList.add(decodeValue(o));
						}
						decodedValue = decodedList;
					} else {
						decodedValue = value;
					}
					return decodedValue;
				}

			};
		}

		@Override
		public void close() throws IOException {
			iterator.close();
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorDAOImpl.class);

	private final GraphDatabaseService database;
	private final DescriptorMapperRegistry registry;
	private final ExecutionEngine executionEngine;

	private final DescriptorCache descriptorCache = new DescriptorCache();

	public DescriptorDAOImpl(DescriptorMapperRegistry registry, GraphDatabaseService database) {
		this.registry = registry;
		this.database = database;
		this.executionEngine = new ExecutionEngine(database);
	}

	@Override
	public <T extends Descriptor> void persist(T descriptor) {
		LOGGER.debug("Creating node for '{}'.", descriptor.getId());
		DescriptorMapper<T> adapter = registry.getDescriptorMapper(descriptor.getClass());
		Node node = database.createNode(adapter.getPrimaryLabel());
		adapter.setId(descriptor, Long.valueOf(node.getId()));
		descriptorCache.put(descriptor);
		this.index(descriptor);
	}

	@Override
	public void flush() {
		LOGGER.debug("Flushing changes to database.");
		for (Descriptor descriptor : descriptorCache.getDescriptors()) {
			LOGGER.debug("Flushing descriptor '{}'.", descriptor.getId());
			Node node = getNode(descriptor);
			DescriptorMapper mapper = registry.getDescriptorMapper(descriptor.getClass());
			flushRelations(descriptor, node, mapper);
			flushProperties(descriptor, node, mapper);
			flushLabels(descriptor, node, mapper);
		}
		this.descriptorCache.flush();
	}

	@Override
	public <T extends Descriptor> T find(Class<T> type, String property, Object value) {
		DescriptorMapper<Descriptor> mapper = registry.getDescriptorMapper(type);
		Node node = null;
		Long id = descriptorCache.findBy(type, property, value);
		if (id != null) {
			node = database.getNodeById(id);
		} else {
			ResourceIterable<Node> nodesByLabelAndProperty = database
					.findNodesByLabelAndProperty(mapper.getPrimaryLabel(), property, value);
			ResourceIterator<Node> iterator = nodesByLabelAndProperty.iterator();
			try {
				if (iterator.hasNext()) {
					node = iterator.next();
				}
			} finally {
				iterator.close();
			}
		}
		return getDescriptor(node);
	}

	/**
	 * Get the {@link Descriptor} which represents the given node.
	 * 
	 * @param node
	 *            The node.
	 * @return The {@link Descriptor}.
	 */
	private <T extends Descriptor> T getDescriptor(Node node) {
		T descriptor = null;
		if (node != null) {
			descriptor = descriptorCache.findBy(node.getId());
			if (descriptor == null) {
				descriptor = createDescriptor(node);
			}
		}
		return descriptor;
	}

	@Override
	public QueryResult executeQuery(String query, Map<String, Object> parameters) {
		ExecutionResult result = executionEngine.execute(query, parameters);
		Iterable<QueryResult.Row> rowIterable = new RowIterable(result.columns(), result.iterator());
		return new QueryResult(result.columns(), rowIterable);
	}

	/**
	 * Flushes the relations of the given descriptor to the {@link Node} it
	 * represents.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @param node
	 *            The node.
	 * @param mapper
	 *            The store.
	 */
	private <T extends Descriptor> void flushRelations(T descriptor, Node node, DescriptorMapper<T> mapper) {
		for (RelationshipType relationshipType : mapper.getRelationshipTypes()) {
			Set<? extends Descriptor> targetDescriptors = mapper.getRelation(descriptor, relationshipType);
			if (targetDescriptors != null && !targetDescriptors.isEmpty()) {
				Set<Node> existingTargetNodes = new HashSet<>();
				Iterable<Relationship> relationships = node.getRelationships(relationshipType, Direction.OUTGOING);
				if (relationships != null) {
					for (Relationship relation : relationships) {
						existingTargetNodes.add(relation.getEndNode());
					}
				}
				for (Descriptor targetDescriptor : targetDescriptors) {
					if (targetDescriptor != null) {
						Node targetNode = getNode(targetDescriptor);
						if (!existingTargetNodes.contains(targetNode)) {
							node.createRelationshipTo(targetNode, relationshipType);
						}
					}
				}
			}
		}
	}

	/**
	 * Flushes the properties of the given descriptor to the {@link Node} it
	 * represents.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @param node
	 *            The node.
	 * @param mapper
	 *            The store.
	 */
	private <T extends Descriptor, P extends Enum> void flushProperties(T descriptor, Node node, DescriptorMapper<T> mapper) {
		for (String propertyName : mapper.getPropertyNames()) {
			Object value = mapper.getProperty(descriptor, propertyName);
			if (value == null) {
				if (node.hasProperty(propertyName)) {
					node.removeProperty(propertyName);
				}
			} else {
				Object existingValue;
				if (node.hasProperty(propertyName)) {
					existingValue = node.getProperty(propertyName);
				} else {
					existingValue = null;
				}
				if (!value.equals(existingValue)) {
					LOGGER.debug("Updating property '" + propertyName + "' with value '" + value + "' on node '" + node.getId() + "'");
					node.setProperty(propertyName, value);
				}
			}
		}
	}

	/**
	 * Flushes the labels of the given descriptor to the {@link Node} it
	 * represents.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @param node
	 *            The node.
	 * @param mapper
	 *            The store.
	 */

	private <T extends Descriptor> void flushLabels(T descriptor, Node node, DescriptorMapper<T> mapper) {
		Set<? extends Label> labels = mapper.getLabels(descriptor);
		for (Label label : labels) {
			node.addLabel(label);
		}
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
	private <T extends Descriptor, R extends Enum & RelationshipType> T createDescriptor(Node node) {
		DescriptorMapper<T> mapper = registry.getDescriptorMapper(node);
		Class<T> type = getType(node);
		T descriptor = mapper.createInstance(type);
		mapper.setId(descriptor, Long.valueOf(node.getId()));
		this.descriptorCache.put(descriptor);
		// create outgoing relationships
		Map<R, Set<Descriptor>> relations = new HashMap<>();
		for (RelationshipType relationshipType : mapper.getRelationshipTypes()) {
			Set<Descriptor> relation = new HashSet<>();
			for (Relationship relationship : node.getRelationships(Direction.OUTGOING, relationshipType)) {
				Node targetNode = relationship.getEndNode();
				Descriptor targetDescriptor = getDescriptor(targetNode);
				relation.add(targetDescriptor);
			}
			mapper.setRelation(descriptor, relationshipType, relation);
		}
		// Set properties
		for (String name : mapper.getPropertyNames()) {
			if (node.hasProperty(name)) {
				mapper.setProperty(descriptor, name, node.getProperty(name));
			}
		}
		// Set labels
		for (Label label : node.getLabels()) {
			mapper.setLabel(descriptor, label);
		}
		this.index(descriptor);
		return descriptor;
	}

	/**
	 * Get the {@link Node} which represents the given descriptor.
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @return The {@link Node}.
	 */
	private <T extends Descriptor> Node getNode(T descriptor) {
		DescriptorMapper<T> mapper = registry.getDescriptorMapper(descriptor.getClass());
		Long id = mapper.getId(descriptor);
		return database.getNodeById(id);
	}

	/**
	 * Return the descriptor type for a given node.
	 * 
	 * @param node
	 *            The node.
	 * @param <T>
	 *            The type.
	 * @return The type.
	 */
	private <T extends Descriptor> Class<T> getType(Node node) {
		// find adapter and create instance
		DescriptorMapper<T> mapper = registry.getDescriptorMapper(node);
		// get labels from node.
		Set<Label> labels = new HashSet<>();
		for (Label label : node.getLabels()) {
			labels.add(label);
		}
		return (Class<T>) mapper.getType(labels);
	}

	private <T extends Descriptor> void index(T descriptor) {
		DescriptorMapper<T> mapper = registry.getDescriptorMapper(descriptor.getClass());
		String indexedProperty = mapper.getPrimaryLabel().getIndexedProperty();
		if (indexedProperty != null) {
			Object value = mapper.getProperty(descriptor, indexedProperty);
			descriptorCache.index(descriptor.getClass(), indexedProperty, value, descriptor.getId());
		}
	}
}
