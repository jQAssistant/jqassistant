package com.buschmais.jqassistant.core.store.impl.dao;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.DescriptorDAO;
import com.buschmais.jqassistant.core.store.api.QueryResult;
import com.buschmais.jqassistant.core.store.api.model.NodeProperty;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.DescriptorMapper;
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
                    Map<String, Object> row = new LinkedHashMap<>();
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
    private final DescriptorMapperRegistry registry;
    private final ExecutionEngine executionEngine;

    private final DescriptorCache descriptorCache = new DescriptorCache();
    @SuppressWarnings("unchecked")
    private final Map<String, Node> nodeCache = new LRUMap(1024);

    public DescriptorDAOImpl(DescriptorMapperRegistry registry, GraphDatabaseService database) {
        this.registry = registry;
        this.database = database;
        this.executionEngine = new ExecutionEngine(database);
    }

    @Override
    public <T extends Descriptor> void persist(T descriptor) {
        LOGGER.debug("Creating node for '{}'.", descriptor.getFullQualifiedName());
        DescriptorMapper<T> adapter = registry.getDescriptorMapper(descriptor.getClass());
        Node node = database.createNode(adapter.getCoreLabel());
        adapter.setId(descriptor, Long.valueOf(node.getId()));
        descriptorCache.put(descriptor);
        nodeCache.put(descriptor.getFullQualifiedName(), node);
    }

    @Override
    public void flush() {
        LOGGER.debug("Flushing changes to database.");
        for (Descriptor descriptor : descriptorCache.getDescriptors()) {
            Node node = findNode(descriptor);
            DescriptorMapper mapper = registry.getDescriptorMapper(descriptor.getClass());
            flushRelations(descriptor, node, mapper);
            flushProperties(descriptor, node, mapper);
            flushLabels(descriptor, node, mapper);
        }
        this.descriptorCache.clear();
    }

    @Override
    public <T extends Descriptor> T find(Class<T> type, String fullQualifiedName) {
        DescriptorMapper<Descriptor> mapper = registry.getDescriptorMapper(type);
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
     * @param node       The node.
     * @param mapper     The mapper.
     */
    private <T extends Descriptor> void flushRelations(T descriptor, Node node, DescriptorMapper<T> mapper) {
        Map<Relation, Set<? extends Descriptor>> relations = mapper.getRelations(descriptor);
        for (Entry<Relation, Set<? extends Descriptor>> relationEntry : relations.entrySet()) {
            Relation relationType = relationEntry.getKey();
            Set<? extends Descriptor> targetDescriptors = relationEntry.getValue();
            if (!targetDescriptors.isEmpty()) {
                Set<Node> existingTargetNodes = new HashSet<Node>();
                Iterable<Relationship> relationships = node.getRelationships(relationType, Direction.OUTGOING);
                if (relationships != null) {
                    for (Relationship relation : relationships) {
                        existingTargetNodes.add(relation.getEndNode());
                    }
                }
                for (Descriptor targetDescriptor : targetDescriptors) {
                    Node targetNode = findNode(targetDescriptor);
                    if (!existingTargetNodes.contains(targetNode)) {
                        node.createRelationshipTo(targetNode, relationType);
                    }
                }
            }
        }
    }

    /**
     * Flushes the properties of the given descriptor to the {@link Node} it
     * represents.
     *
     * @param descriptor The descriptor.
     * @param node       The node.
     * @param mapper     The mapper.
     */
    private <T extends Descriptor> void flushProperties(T descriptor, Node node, DescriptorMapper<T> mapper) {
        Map<NodeProperty, Object> properties = mapper.getProperties(descriptor);
        for (Entry<NodeProperty, Object> entry : properties.entrySet()) {
            NodeProperty property = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                LOGGER.debug("Setting property '" + property + "' with value '" + value + "' on node '" + node.getId() + "'");
                node.setProperty(property.name(), value);
            }
        }
    }

    /**
     * Flushes the labels of the given descriptor to the {@link Node} it
     * represents.
     *
     * @param descriptor The descriptor.
     * @param node       The node.
     * @param mapper     The mapper.
     */
    private <T extends Descriptor> void flushLabels(T descriptor, Node node, DescriptorMapper<T> mapper) {
        Set<Label> labels = mapper.getLabels(descriptor);
        for (Label label : labels) {
            node.addLabel(label);
        }
    }

    /**
     * Find the {@link Node} which represents the given descriptor.
     *
     * @param descriptor The descriptor.
     * @return The {@link Node}.
     */
    private <T extends Descriptor> Node findNode(T descriptor) {
        DescriptorMapper<T> mapper = registry.getDescriptorMapper(descriptor.getClass());
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
    private <T extends Descriptor> T getDescriptor(Node node) {
        T descriptor = this.descriptorCache.findBy(node.getId());
        if (descriptor == null) {
            // find adapter and create instance
            Class<T> type = (Class<T>) registry.getDescriptorMapper(node).getJavaType();
            DescriptorMapper<T> mapper = registry.getDescriptorMapper(type);
            descriptor = mapper.createInstance();
            mapper.setId(descriptor, Long.valueOf(node.getId()));
            descriptor.setFullQualifiedName((String) node.getProperty(NodeProperty.FQN.name()));
            this.descriptorCache.put(descriptor);
            this.nodeCache.put(descriptor.getFullQualifiedName(), node);
            // create outgoing relationships
            Map<Relation, Set<Descriptor>> relations = new HashMap<Relation, Set<Descriptor>>();
            for (Relationship relationship : node.getRelationships(Direction.OUTGOING)) {
                Relation relation = Relation.getRelation(relationship.getType().name());
                if (relation != null) {
                    Node targetNode = relationship.getEndNode();
                    Descriptor targetDescriptor = getDescriptor(targetNode);
                    Set<Descriptor> set = relations.get(relation);
                    if (set == null) {
                        set = new HashSet<Descriptor>();
                        relations.put(relation, set);
                    }
                    set.add(targetDescriptor);
                }
            }
            mapper.setRelations(descriptor, relations);
            // Set properties
            for (String name : node.getPropertyKeys()) {
                NodeProperty nodeProperty = NodeProperty.getProperty(name);
                if (nodeProperty != null) {
                    mapper.setProperty(descriptor, nodeProperty, node.getProperty(name));
                }
            }
            // Set labels
            for (Label label : node.getLabels()) {
                mapper.setLabel(descriptor, label);
            }
        }
        return descriptor;
    }
}
