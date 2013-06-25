package com.buschmais.jqassistant.store.impl.dao;

import java.io.Closeable;
import java.io.IOException;
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
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.store.api.DescriptorDAO;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.QueryResult;
import com.buschmais.jqassistant.store.impl.dao.mapper.DescriptorMapper;
import com.buschmais.jqassistant.store.impl.model.RelationType;

public class DescriptorDAOImpl implements DescriptorDAO {

    private final class RowIterable implements Iterable<Map<String, Object>>, Closeable {

        private ResourceIterator<Map<String, Object>> iterator;

        private RowIterable(ResourceIterator<Map<String, Object>> iterator) {
            this.iterator = iterator;
        }

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
                        return getDescriptor(node);
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

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DescriptorDAOImpl.class);

    private final GraphDatabaseService database;
    private final DescriptorAdapterRegistry registry;
    private final ExecutionEngine executionEngine;

    private final DescriptorCache descriptorCache = new DescriptorCache();
    private final Map<String, Node> nodeCache = new HashMap<String, Node>();

    public DescriptorDAOImpl(DescriptorAdapterRegistry registry,
                             GraphDatabaseService database) {
        this.registry = registry;
        this.database = database;
        this.executionEngine = new ExecutionEngine(database);
    }

    @Override
    public <T extends AbstractDescriptor> void persist(T descriptor) {
        LOGGER.debug("Creating node for '{}'.",
                descriptor.getFullQualifiedName());
        DescriptorMapper<T> adapter = registry.getDescriptorAdapter(descriptor
                .getClass());
        Node node = database.createNode(adapter.getCoreLabel());
        adapter.setId(descriptor, Long.valueOf(node.getId()));
        node.setProperty(NodeProperty.FQN.name(),
                descriptor.getFullQualifiedName());
        descriptorCache.put(descriptor, node);
        nodeCache.put(descriptor.getFullQualifiedName(), node);
    }

    @Override
    public void flush() {
        for (AbstractDescriptor descriptor : descriptorCache.getDescriptors()) {
            flushRelations(descriptor);
        }
    }

    @Override
    public <T extends AbstractDescriptor> T find(Class<T> type,
                                                 String fullQualifiedName) {
        DescriptorMapper<AbstractDescriptor> mapper = registry
                .getDescriptorAdapter(type);
        Node node = nodeCache.get(fullQualifiedName);
        if (node == null) {
            ResourceIterable<Node> nodesByLabelAndProperty = database
                    .findNodesByLabelAndProperty(mapper.getCoreLabel(),
                            NodeProperty.FQN.name(), fullQualifiedName);
            ResourceIterator<Node> iterator = nodesByLabelAndProperty
                    .iterator();
            try {
                if (node == null && iterator.hasNext()) {
                    node = iterator.next();
                    nodeCache.put(fullQualifiedName, node);
                }
            } finally {
                iterator.close();
            }
        }
        if (node != null) {
            return getDescriptor(node);
        }
        return null;
    }

    @Override
    public QueryResult executeQuery(String query, Map<String, Object> parameters) {
        ExecutionResult result = executionEngine.execute(query, parameters);
        Iterable<Map<String, Object>> rowIterable = new RowIterable(result.iterator());
        return new QueryResult(result.columns(), rowIterable);
    }

    /**
     * Flushes the relations of the given descriptor to the {@link Node} it
     * represents.
     *
     * @param descriptor The descriptor.
     */
    private <T extends AbstractDescriptor> void flushRelations(T descriptor) {
        Node node = findNode(descriptor);
        DescriptorMapper<T> adapter = registry.getDescriptorAdapter(descriptor
                .getClass());
        Map<RelationType, Set<? extends AbstractDescriptor>> relations = adapter
                .getRelations(descriptor);
        for (Entry<RelationType, Set<? extends AbstractDescriptor>> relationEntry : relations
                .entrySet()) {
            RelationType relationType = relationEntry.getKey();
            Set<Node> existingTargetNodes = new HashSet<Node>();
            Iterable<Relationship> relationships = node.getRelationships(
                    relationType, Direction.OUTGOING);
            if (relationships != null) {
                for (Relationship relation : relationships) {
                    existingTargetNodes.add(relation.getEndNode());
                }
            }
            Set<? extends AbstractDescriptor> targetDescriptors = relationEntry
                    .getValue();
            for (AbstractDescriptor targetDescriptor : targetDescriptors) {
                Node targetNode = findNode(targetDescriptor);
                if (!existingTargetNodes.contains(targetNode)) {
                    node.createRelationshipTo(targetNode, relationType);
                }
            }
        }
    }

    /**
     * Find the {@link Node} which represents the given descriptor.
     *
     * @param descriptor The descriptor.
     * @return The {@link Node}.
     */
    private <T extends AbstractDescriptor> Node findNode(T descriptor) {
        DescriptorMapper<T> mapper = registry.getDescriptorAdapter(descriptor
                .getClass());
        Long id = mapper.getId(descriptor);
        return database.getNodeById(id);
    }

    /**
     * Creates a descriptor instance from the given {@link Node}.
     * <p>
     * A new descriptor instance is created if no matching one can be found in
     * the {@link #descriptorCache}.
     * </p>
     *
     * @param node The {@link Node}.
     * @return The descriptor.
     */
    @SuppressWarnings("unchecked")
    private <T extends AbstractDescriptor> T getDescriptor(Node node) {
        T descriptor = this.descriptorCache.findBy(node);
        if (descriptor == null) {
            // find adapter and create instance
            Class<T> type = (Class<T>) registry.getDescriptorAdapter(node)
                    .getJavaType();
            DescriptorMapper<T> mapper = registry.getDescriptorAdapter(type);
            descriptor = mapper.createInstance();
            mapper.setId(descriptor, Long.valueOf(node.getId()));
            descriptor.setFullQualifiedName((String) node
                    .getProperty(NodeProperty.FQN.name()));
            this.descriptorCache.put(descriptor, node);
            this.nodeCache.put(descriptor.getFullQualifiedName(), node);
            // create outgoing relationships
            Map<RelationType, Set<AbstractDescriptor>> relations = new HashMap<RelationType, Set<AbstractDescriptor>>();
            for (Relationship relationship : node
                    .getRelationships(Direction.OUTGOING)) {
                Node targetNode = relationship.getEndNode();
                AbstractDescriptor targetDescriptor = getDescriptor(targetNode);
                RelationType relationType = RelationType.valueOf(relationship
                        .getType().name());
                Set<AbstractDescriptor> set = relations.get(relationType);
                if (set == null) {
                    set = new HashSet<AbstractDescriptor>();
                    relations.put(relationType, set);
                }
                set.add(targetDescriptor);
            }
            mapper.setRelations(descriptor, relations);
        }
        return descriptor;
    }
}
