package com.buschmais.jqassistant.core.store.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.neo4j.kernel.GraphDatabaseAPI;

import com.buschmais.cdo.api.CdoManager;
import com.buschmais.cdo.api.CdoManagerFactory;
import com.buschmais.cdo.api.bootstrap.CdoUnit;
import com.buschmais.cdo.neo4j.impl.EmbeddedNeo4jCdoManagerFactoryImpl;
import com.buschmais.cdo.neo4j.impl.datastore.EmbeddedNeo4jDatastoreSession;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
public class EmbeddedGraphStore extends AbstractGraphStore {

    /**
     * The directory of the database.
     */
    private final String databaseDirectory;

    /**
     * Constructor.
     *
     * @param databaseDirectory The directory of the database.
     */
    public EmbeddedGraphStore(String databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    @Override
    protected GraphDatabaseAPI getDatabaseAPI(CdoManager cdoManager) {
        return (GraphDatabaseAPI) cdoManager.getDatastoreSession(EmbeddedNeo4jDatastoreSession.class).getGraphDatabaseService();
    }

    @Override
    protected CdoManagerFactory createCdoManagerFactory(Collection<Class<?>> types) {
        File database = new File(databaseDirectory);
        try {
            return new EmbeddedNeo4jCdoManagerFactoryImpl(new CdoUnit(null, null, database.toURI().toURL(), null, new HashSet<>(types),
                    CdoUnit.ValidationMode.NONE, CdoUnit.TransactionAttribute.MANDATORY, new Properties()));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot create CdoManagerFactory.", e);
        }
    }

    @Override
    protected void closeCdoManagerFactory(CdoManagerFactory cdoManagerFactory) {
        cdoManagerFactory.close();
    }

}
