package com.buschmais.jqassistant.store.impl.dao;

import com.buschmais.jqassistant.store.api.DescriptorDAO;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.model.QueryResult;
import com.buschmais.jqassistant.store.impl.dao.mapper.DescriptorMapper;
import com.buschmais.jqassistant.store.impl.model.RelationType;
import org.apache.commons.collections.map.LRUMap;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class DescriptorDAOImpl implements DescriptorDAO {

    private final class RowIterable implements Iterable<QueryResult.Row>, Closeable {

        private ResourceIterator<Map<String, Object>> iterator;

        private RowIterable(ResourceIterator<Map<String, Object>> iterator) {
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
                    Map<String, Object> row = new HashMap<String, Object>();
                    for (Entry<String, Object> entry : iterator.next().entrySet()) {
                        String name = entry.getKey();
                        Object value = entry.getValue();
                        Object decodedValue = decodeValue(value);
                        row.put(name, decodedValue);
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
    private final DescriptorAdapterRegistry registry;
    private final ExecutionEngine executionEngine;

    private final DescriptorCache descriptorCache = new DescriptorCache();
    private final Map<String, Node> nodeCache = new LRUMap();

    public DescriptorDAOImpl(DescriptorAdapterRegistry registry, GraphDatabaseService database) {
        this.registry = registry;
        this.database = database;
        this.executionEngine = new ExecutionEngine(database);
    }

    @Override
    public <T extends AbstractDescriptor> void persist(T descriptor) {
        LOGGER.debug("Creating node for '{}'.", descriptor.getFullQualifiedName());
        DescriptorMapper<T> adapter = registry.getDescriptorAdapter(descriptor.getClass());
        Node node = database.createNode(adapter.getCoreLabel());
        adapter.setId(descriptor, Long.valueOf(node.getId()));
        node.setProperty(NodeProperty.FQN.name(), descriptor.getFullQualifiedName());
        descriptorCache.put(descriptor);
        nodeCache.put(descriptor.getFullQualifiedName(), node);
    }

    @Override
    public void flush() {
        LOGGER.debug("Flushing changes to database.");
        for (AbstractDescriptor descriptor : descriptorCache.getDescriptors()) {
            flushRelations(descriptor);
        }
        this.descriptorCache.clear();
    }

    @Override
    public <T extends AbstractDescriptor> T find(Class<T> type, String fullQualifiedName) {
        DescriptorMapper<AbstractDescriptor> mapper = registry.getDescriptorAdapter(type);
        Node node = nodeCache.get(fullQualifiedName);
        if (node == null) {
            ResourceIterable<Node> nodesByLabelAndProperty = database.findNodesByLabelAndProperty(mapper.getCoreLabel(), NodeProperty.FQN.name(), fullQualifiedName);
            ResourceIterator<Node> iterator = nodesByLabelAndProperty.iterator();
            try {
                if (iterator.hasNext()) {
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
        Iterable<QueryResult.Row> rowIterable = new RowIterable(result.iterator());
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
        DescriptorMapper<T> adapter = registry.getDescriptorAdapter(descriptor.getClass());
        Map<RelationType, Set<? extends AbstractDescriptor>> relations = adapter.getRelations(descriptor);
        for (Entry<RelationType, Set<? extends AbstractDescriptor>> relationEntry : relations.entrySet()) {
            RelationType relationType = relationEntry.getKey();
            Set<? extends AbstractDescriptor> targetDescriptors = relationEntry.getValue();
            if (!targetDescriptors.isEmpty()) {
                Set<Node> existingTargetNodes = new HashSet<Node>();
                Iterable<Relationship> relationships = node.getRelationships(relationType, Direction.OUTGOING);
                if (relationships != null) {
                    for (Relationship relation : relationships) {
                        existingTargetNodes.add(relation.getEndNode());
                    }
                }
                for (AbstractDescriptor targetDescriptor : targetDescriptors) {
                    Node targetNode = findNode(targetDescriptor);
                    if (!existingTargetNodes.contains(targetNode)) {
                        node.createRelationshipTo(targetNode, relationType);
                    }
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
        DescriptorMapper<T> mapper = registry.getDescriptorAdapter(descriptor.getClass());
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
        T descriptor = this.descriptorCache.findBy(node.getId());
        if (descriptor == null) {
            // find adapter and create instance
            Class<T> type = (Class<T>) registry.getDescriptorAdapter(node).getJavaType();
            DescriptorMapper<T> mapper = registry.getDescriptorAdapter(type);
            descriptor = mapper.createInstance();
            mapper.setId(descriptor, Long.valueOf(node.getId()));
            descriptor.setFullQualifiedName((String) node.getProperty(NodeProperty.FQN.name()));
            this.descriptorCache.put(descriptor);
            this.nodeCache.put(descriptor.getFullQualifiedName(), node);
            // create outgoing relationships
            Map<RelationType, Set<AbstractDescriptor>> relations = new HashMap<RelationType, Set<AbstractDescriptor>>();
            for (Relationship relationship : node.getRelationships(Direction.OUTGOING)) {
                Node targetNode = relationship.getEndNode();
                AbstractDescriptor targetDescriptor = getDescriptor(targetNode);
                RelationType relationType = RelationType.valueOf(relationship.getType().name());
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
