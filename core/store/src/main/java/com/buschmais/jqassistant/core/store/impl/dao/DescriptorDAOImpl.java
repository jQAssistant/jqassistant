package com.buschmais.jqassistant.core.store.impl.dao;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.DescriptorDAO;
import com.buschmais.jqassistant.core.store.api.QueryResult;
import com.buschmais.jqassistant.core.store.api.model.NodeProperty;
import com.buschmais.jqassistant.core.store.api.model.Relation;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.DescriptorMapper;
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
        LOGGER.debug("Creating node for '{}'.", descriptor.getFullQualifiedName());
        DescriptorMapper<T> adapter = registry.getDescriptorMapper(descriptor.getClass());
        Node node = database.createNode(adapter.getCoreLabel());
        adapter.setId(descriptor, Long.valueOf(node.getId()));
        descriptorCache.put(descriptor);
    }

    @Override
    public void flush() {
        LOGGER.debug("Flushing changes to database.");
        for (Descriptor descriptor : descriptorCache.getDescriptors()) {
            LOGGER.debug("Flushing descriptor '{}'.", descriptor.getFullQualifiedName());
            Node node = getNode(descriptor);
            DescriptorMapper mapper = registry.getDescriptorMapper(descriptor.getClass());
            flushRelations(descriptor, node, mapper);
            flushProperties(descriptor, node, mapper);
            flushLabels(descriptor, node, mapper);
        }
        this.descriptorCache.flush();
    }

    @Override
    public <T extends Descriptor> T find(Class<T> type, String fullQualifiedName) {
        DescriptorMapper<Descriptor> mapper = registry.getDescriptorMapper(type);
        Node node = null;
        Long id = descriptorCache.findBy(fullQualifiedName);
        if (id != null) {
            node = database.getNodeById(id);
        } else {
            ResourceIterable<Node> nodesByLabelAndProperty = database.findNodesByLabelAndProperty(mapper.getCoreLabel(), NodeProperty.FQN.name(), fullQualifiedName);
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
     * @param node The node.
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
                Set<Node> existingTargetNodes = new HashSet<>();
                Iterable<Relationship> relationships = node.getRelationships(relationType, Direction.OUTGOING);
                if (relationships != null) {
                    for (Relationship relation : relationships) {
                        existingTargetNodes.add(relation.getEndNode());
                    }
                }
                for (Descriptor targetDescriptor : targetDescriptors) {
                    if (targetDescriptor != null) {
                        Node targetNode = getNode(targetDescriptor);
                        if (!existingTargetNodes.contains(targetNode)) {
                            node.createRelationshipTo(targetNode, relationType);
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
     * @param descriptor The descriptor.
     * @param node       The node.
     * @param mapper     The mapper.
     */
    private <T extends Descriptor> void flushProperties(T descriptor, Node node, DescriptorMapper<T> mapper) {
        Map<NodeProperty, Object> properties = mapper.getProperties(descriptor);
        for (Entry<NodeProperty, Object> entry : properties.entrySet()) {
            NodeProperty property = entry.getKey();
            String name = property.name();
            Object value = entry.getValue();
            if (value == null) {
                if (node.hasProperty(name)) {
                    node.removeProperty(name);
                }
            } else {
                Object existingValue;
                if (node.hasProperty(property.name())) {
                    existingValue = node.getProperty(name);
                } else {
                    existingValue = null;
                }
                if (!value.equals(existingValue)) {
                    LOGGER.debug("Updating property '" + property + "' with value '" + value + "' on node '" + node.getId() + "'");
                    node.setProperty(name, value);
                }
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
     * Creates a descriptor instance from the given {@link Node}.
     * <p>
     * A new descriptor instance is created if no matching one can be found in
     * the {@link #descriptorCache}.
     * </p>
     *
     * @param node The {@link Node}.
     * @return The descriptor.
     */
    private <T extends Descriptor> T createDescriptor(Node node) {
        DescriptorMapper<T> mapper = registry.getDescriptorMapper(node);
        Class<T> type = getType(node);
        T descriptor = mapper.createInstance(type);
        mapper.setId(descriptor, Long.valueOf(node.getId()));
        descriptor.setFullQualifiedName((String) node.getProperty(NodeProperty.FQN.name()));
        this.descriptorCache.put(descriptor);
        // create outgoing relationships
        Map<Relation, Set<Descriptor>> relations = new HashMap<>();
        for (Relationship relationship : node.getRelationships(Direction.OUTGOING)) {
            Relation relation = Relation.getRelation(relationship.getType().name());
            if (relation != null) {
                Node targetNode = relationship.getEndNode();
                Descriptor targetDescriptor = getDescriptor(targetNode);
                Set<Descriptor> set = relations.get(relation);
                if (set == null) {
                    set = new HashSet<>();
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
        return descriptor;
    }

    /**
     * Get the {@link Node} which represents the given descriptor.
     *
     * @param descriptor The descriptor.
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
     * @param node The node.
     * @param <T>  The type.
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
}
