package com.buschmais.jqassistant.core.store.impl;

import com.buschmais.cdo.api.CdoManager;
import com.buschmais.cdo.api.CdoManagerFactory;
import com.buschmais.cdo.api.TransactionAttribute;
import com.buschmais.cdo.api.ValidationMode;
import com.buschmais.cdo.api.bootstrap.Cdo;
import com.buschmais.cdo.neo4j.api.Neo4jCdoProvider;
import com.buschmais.cdo.neo4j.impl.datastore.EmbeddedNeo4jDatastoreSession;
import com.buschmais.jqassistant.core.store.api.Store;
import org.neo4j.kernel.GraphDatabaseAPI;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Properties;

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
            return Cdo.createCdoManagerFactory(database.toURI().toURL(), Neo4jCdoProvider.class, types.toArray(new Class<?>[0]),
                    ValidationMode.NONE, TransactionAttribute.MANDATORY, new Properties());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot create CdoManagerFactory.", e);
        }
    }

    @Override
    protected void closeCdoManagerFactory(CdoManagerFactory cdoManagerFactory) {
        cdoManagerFactory.close();
    }

}
