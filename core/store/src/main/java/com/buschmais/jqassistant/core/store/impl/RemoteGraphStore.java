package com.buschmais.jqassistant.core.store.impl;

import java.util.Collection;
import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;

import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.remote.api.Neo4jRemoteStoreProvider;

public class RemoteGraphStore extends AbstractGraphStore {

    public RemoteGraphStore(StoreConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected GraphDatabaseService getGraphDatabaseService(XOManager xoManager) {
        throw new IllegalStateException("Not Supported.");
    }

    @Override
    protected XOManagerFactory createXOManagerFactory(Collection<Class<?>> types) {
        XOUnit xoUnit = getXoUnit(types);
        return XO.createXOManagerFactory(xoUnit);
    }

    @Override
    protected void closeXOManagerFactory(XOManagerFactory factory) {
        factory.close();
    }

    @Override
    protected int getAutocommitThreshold() {
        return 512;
    }

    private XOUnit getXoUnit(Collection<Class<?>> types) {
        Properties properties = new Properties();
        String username = storeConfiguration.getUsername();
        if (username != null) {
            properties.setProperty("neo4j.remote.username", username);
        }
        String password = storeConfiguration.getPassword();
        if (password != null) {
            properties.setProperty("neo4j.remote.password", password);
        }
        String encryptionLevel = storeConfiguration.getEncryptionLevel();
        if (encryptionLevel != null) {
            properties.setProperty("neo4j.remote.encryptionLevel", encryptionLevel);
        }
        return XOUnit.builder().uri(storeConfiguration.getUri()).provider(Neo4jRemoteStoreProvider.class).types(types).properties(properties)
                .mappingConfiguration(XOUnit.MappingConfiguration.builder().strictValidation(true).build()).build();
    }
}
