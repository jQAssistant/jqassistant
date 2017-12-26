package com.buschmais.jqassistant.core.store.impl;

import java.util.Properties;

import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider.Property;

import org.neo4j.graphdb.GraphDatabaseService;

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
        return 2048;
    }

    @Override
    protected void configure(XOUnit.XOUnitBuilder builder) {
        builder.provider(RemoteNeo4jXOProvider.class);
        Properties properties = new Properties();
        String username = storeConfiguration.getUsername();
        if (username != null) {
            properties.setProperty(Property.USERNAME.getKey(), username);
        }
        String password = storeConfiguration.getPassword();
        if (password != null) {
            properties.setProperty(Property.PASSWORD.getKey(), password);
        }
        String encryptionLevel = storeConfiguration.getEncryptionLevel();
        if (encryptionLevel != null) {
            properties.setProperty(Property.ENCRYPTION_LEVEL.getKey(), encryptionLevel);
        }
        Properties storeConfigurationProperties = storeConfiguration.getProperties();
        if (storeConfigurationProperties != null) {
            properties.putAll(storeConfigurationProperties);
        }
        builder.properties(properties);
    }
}
