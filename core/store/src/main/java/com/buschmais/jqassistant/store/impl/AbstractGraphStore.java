package com.buschmais.jqassistant.store.impl;

import com.buschmais.jqassistant.store.api.DescriptorDAO;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.*;
import com.buschmais.jqassistant.store.impl.dao.DescriptorAdapterRegistry;
import com.buschmais.jqassistant.store.impl.dao.DescriptorDAOImpl;
import com.buschmais.jqassistant.store.impl.dao.mapper.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;

import java.util.Collections;
import java.util.Map;

import static com.buschmais.jqassistant.store.api.DescriptorDAO.NodeProperty.FQN;

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
    private DescriptorAdapterRegistry adapterRegistry;

    /**
     * The {@link DescriptorDAO} instance to use.
     */
    private DescriptorDAO descriptorDAO;

    @Override
    public void start() {
        database = startDatabase();
        for (DescriptorDAO.CoreLabel label : DescriptorDAO.CoreLabel.values()) {
            database.schema().indexFor(label).on(FQN.name());
        }
        adapterRegistry = new DescriptorAdapterRegistry();
        adapterRegistry.register(new PackageDescriptorMapper());
        adapterRegistry.register(new ClassDescriptorMapper());
        adapterRegistry.register(new MethodDescriptorMapper());
        adapterRegistry.register(new FieldDescriptorMapper());

        descriptorDAO = new DescriptorDAOImpl(adapterRegistry, database);
    }

    @Override
    public void stop() {
        adapterRegistry = null;
        stopDatabase(database);
    }

    public GraphDatabaseAPI getDatabaseAPI() {
        if (database == null) {
            throw new IllegalStateException("Store is not started!.");
        }
        return (GraphDatabaseAPI) database;
    }

    @Override
    public PackageDescriptor resolvePackageDescriptor(final PackageDescriptor parentPackageDescriptor, final String packageName) {
        final Name name = new Name(parentPackageDescriptor, '.', packageName);
        PackageDescriptor packageDescriptor = descriptorDAO.find(PackageDescriptor.class, name.getFullQualifiedName());
        if (packageDescriptor == null) {
            packageDescriptor = new PackageDescriptor();
            packageDescriptor.setFullQualifiedName(name.getFullQualifiedName());
            descriptorDAO.persist(packageDescriptor);
        }
        return packageDescriptor;
    }

    @Override
    public ClassDescriptor resolveClassDescriptor(final PackageDescriptor packageDescriptor, final String className) {
        final Name name = new Name(packageDescriptor, '.', className);
        ClassDescriptor classDescriptor = descriptorDAO.find(ClassDescriptor.class, name.getFullQualifiedName());
        if (classDescriptor == null) {
            classDescriptor = new ClassDescriptor();
            classDescriptor.setFullQualifiedName(name.getFullQualifiedName());
            descriptorDAO.persist(classDescriptor);
        }
        return classDescriptor;
    }

    @Override
    public MethodDescriptor resolveMethodDescriptor(final ClassDescriptor classDescriptor, String methodName) {
        final Name name = new Name(classDescriptor, '#', methodName);
        MethodDescriptor methodDescriptor = new MethodDescriptor();
        methodDescriptor.setFullQualifiedName(name.getFullQualifiedName());
        descriptorDAO.persist(methodDescriptor);
        return methodDescriptor;
    }

    @Override
    public FieldDescriptor resolveFieldDescriptor(final ClassDescriptor classDescriptor, String fieldName) {
        final Name name = new Name(classDescriptor, '#', fieldName);
        FieldDescriptor fieldDescriptor = new FieldDescriptor();
        fieldDescriptor.setFullQualifiedName(name.getFullQualifiedName());
        descriptorDAO.persist(fieldDescriptor);
        return fieldDescriptor;
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

    protected DescriptorAdapterRegistry getAdapterRegistry() {
        return adapterRegistry;
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