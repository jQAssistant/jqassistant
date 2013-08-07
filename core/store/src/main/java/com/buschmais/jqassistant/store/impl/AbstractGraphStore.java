package com.buschmais.jqassistant.store.impl;

import com.buschmais.jqassistant.core.model.api.descriptor.*;
import com.buschmais.jqassistant.store.api.DescriptorDAO;
import com.buschmais.jqassistant.store.api.QueryResult;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.NodeLabel;
import com.buschmais.jqassistant.store.impl.dao.DescriptorMapperRegistry;
import com.buschmais.jqassistant.store.impl.dao.DescriptorDAOImpl;
import com.buschmais.jqassistant.store.impl.dao.mapper.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;

import java.util.Collections;
import java.util.Map;

import static com.buschmais.jqassistant.store.api.model.NodeProperty.FQN;

/**
 * Abstract base implementation of a {@link Store}.
 * <p>
 * Provides methods for managing the life cycle of a store, transactions,
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
    public void start() {
        database = startDatabase();
        for (NodeLabel label : NodeLabel.values()) {
            database.schema().indexFor(label).on(FQN.name());
        }
        mapperRegistry = new DescriptorMapperRegistry();
        mapperRegistry.register(new ArtifactDescriptorMapper());
        mapperRegistry.register(new PackageDescriptorMapper());
        mapperRegistry.register(new TypeDescriptorMapper());
        mapperRegistry.register(new MethodDescriptorMapper());
        mapperRegistry.register(new FieldDescriptorMapper());
        descriptorDAO = new DescriptorDAOImpl(mapperRegistry, database);
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
    public ArtifactDescriptor createArtifactDescriptor(final String fullQualifiedName) {
        return persist(new ArtifactDescriptor(), new Name(fullQualifiedName));
    }

    @Override
    public ArtifactDescriptor findArtifactDescriptor(String fullQualifiedName) {
        return descriptorDAO.find(ArtifactDescriptor.class, fullQualifiedName);
    }

    @Override
    public PackageDescriptor createPackageDescriptor(final ArtifactDescriptor parentArtifactDescriptor, final String packageName) {
        return persist(new PackageDescriptor(), new Name(parentArtifactDescriptor, '/', packageName));
    }

    @Override
    public PackageDescriptor createPackageDescriptor(final PackageDescriptor parentPackageDescriptor, final String packageName) {
        return persist(new PackageDescriptor(), new Name(parentPackageDescriptor, '.', packageName));
    }

    @Override
    public PackageDescriptor findPackageDescriptor(String fullQualifiedName) {
        return descriptorDAO.find(PackageDescriptor.class, fullQualifiedName);
    }

    @Override
    public TypeDescriptor createClassDescriptor(final PackageDescriptor packageDescriptor, final String className) {
        return persist(new TypeDescriptor(), new Name(packageDescriptor, '.', className));
    }

    @Override
    public TypeDescriptor findClassDescriptor(String fullQualifiedName) {
        return descriptorDAO.find(TypeDescriptor.class, fullQualifiedName);
    }

    @Override
    public MethodDescriptor createMethodDescriptor(final TypeDescriptor typeDescriptor, String methodName) {
        return persist(new MethodDescriptor(), new Name(typeDescriptor, '#', methodName));
    }

    @Override
    public FieldDescriptor createFieldDescriptor(final TypeDescriptor typeDescriptor, String fieldName) {
        return persist(new FieldDescriptor(), new Name(typeDescriptor, '#', fieldName));
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

    private <T extends AbstractDescriptor> T persist(T descriptor, Name name) {
        descriptor.setFullQualifiedName(name.getFullQualifiedName());
        descriptorDAO.persist(descriptor);
        return descriptor;
    }

}