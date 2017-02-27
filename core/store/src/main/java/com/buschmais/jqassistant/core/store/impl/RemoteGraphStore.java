package com.buschmais.jqassistant.core.store.impl;

import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;

import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider;

public class RemoteGraphStore extends AbstractGraphStore {

    public RemoteGraphStore(StoreConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected GraphDatabaseService getGraphDatabaseService(XOManager xoManager) {
        throw new IllegalStateException("Not Supported.");
    }

    @Override
    protected int getAutocommitThreshold() {
        return 1024;
    }

    @Override
    protected void configure(XOUnit.XOUnitBuilder builder) {
        builder.provider(RemoteNeo4jXOProvider.class);
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
        Properties storeConfigurationProperties = storeConfiguration.getProperties();
        if (storeConfigurationProperties != null) {
            properties.putAll(storeConfigurationProperties);
        }
        builder.properties(properties);
    }
}
