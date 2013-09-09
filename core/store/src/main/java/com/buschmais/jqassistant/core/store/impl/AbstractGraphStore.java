package com.buschmais.jqassistant.core.store.impl;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.DescriptorDAO;
import com.buschmais.jqassistant.core.store.api.QueryResult;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;
import com.buschmais.jqassistant.core.store.impl.dao.DescriptorDAOImpl;
import com.buschmais.jqassistant.core.store.impl.dao.DescriptorMapperRegistry;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.DescriptorMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.kernel.GraphDatabaseAPI;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.buschmais.jqassistant.core.store.api.model.IndexProperty.FQN;

/**
 * Abstract base implementation of a {@link Store}.
 * <p>
 * Provides methods for managing the life packages of a store, transactions,
 * resolving descriptors and executing CYPHER queries.
 * </p>
 */
public abstract class AbstractGraphStore implements Store {

    /**
     * The {@link GraphDatabaseService} to use.
     */
    protected GraphDatabaseService database;

    /**
     * The registry of {@link DescriptorMapper}s. These are used to resolve
     * required metadata.
     */
    private DescriptorMapperRegistry mapperRegistry;

    /**
     * The {@link DescriptorDAO} instance to use.
     */
    private DescriptorDAO descriptorDAO;

    @Override
    public void start(List<DescriptorMapper<?>> mappers) {
        database = startDatabase();
        Set<IndexedLabel> indexedLabels = new HashSet<>();
        mapperRegistry = new DescriptorMapperRegistry();
        for (DescriptorMapper<?> mapper : mappers) {
            if (!indexedLabels.add(mapper.getPrimaryLabel())) {
                throw new IllegalStateException("Primary label is already defined " + mapper.getPrimaryLabel() + ":" + indexedLabels);
            }
            mapperRegistry.register(mapper);
        }
        descriptorDAO = new DescriptorDAOImpl(mapperRegistry, database);
        beginTransaction();
        for (IndexedLabel label : indexedLabels) {
            IndexDefinition index = null;
            for (IndexDefinition indexDefinition : database.schema().getIndexes(label)) {
                for (String s : indexDefinition.getPropertyKeys()) {
                    if (FQN.name().equals(s)) {
                        index = indexDefinition;
                    }
                }
            }
            if (label.isIndexed() && index == null) {
                database.schema().indexFor(label).on(FQN.name()).create();
            } else if (!label.isIndexed() && index != null) {
                index.drop();
            }
        }
        commitTransaction();
    }

    @Override
    public void stop() {
        mapperRegistry = null;
        stopDatabase(database);
    }

    public GraphDatabaseAPI getDatabaseAPI() {
        if (database == null) {
            throw new IllegalStateException("Store is not started!.");
        }
        return (GraphDatabaseAPI) database;
    }

    @Override
    public <T extends Descriptor> T create(Class<T> type, String fullQualifiedName) {
        if (fullQualifiedName == null) {
            throw new IllegalArgumentException("FQN must not be null.");
        }
        DescriptorMapper<T> mapper = mapperRegistry.getDescriptorMapper(type);
        T descriptor = mapper.createInstance(type);
        descriptor.setFullQualifiedName(fullQualifiedName);
        descriptorDAO.persist(descriptor);
        return descriptor;
    }

    @Override
    public <T extends Descriptor> T find(Class<T> type, String fullQualifiedName) {
        return descriptorDAO.find(type, fullQualifiedName);
    }

    @Override
    public QueryResult executeQuery(String query) {
        return descriptorDAO.executeQuery(query, Collections.<String, Object>emptyMap());
    }

    @Override
    public QueryResult executeQuery(String query, Map<String, Object> parameters) {
        return descriptorDAO.executeQuery(query, parameters);
    }

    @Override
    public void flush() {
        descriptorDAO.flush();
    }

    protected DescriptorMapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    /**
     * Delegates to the sub class to start the database.
     *
     * @return The {@link GraphDatabaseService} instance to use.
     */
    protected abstract GraphDatabaseService startDatabase();

    /**
     * Delegates to the sub class to stop the database.
     *
     * @param database The used {@link GraphDatabaseService} instance.
     */
    protected abstract void stopDatabase(GraphDatabaseService database);


}