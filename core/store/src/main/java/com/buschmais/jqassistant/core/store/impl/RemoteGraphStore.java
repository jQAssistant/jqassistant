package com.buschmais.jqassistant.core.store.impl;

import java.util.Properties;

import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider.Property;

public class RemoteGraphStore extends AbstractGraphStore {

    public RemoteGraphStore(StoreConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected int getAutocommitThreshold() {
        return 2048;
    }

    @Override
    protected XOUnit configure(XOUnit.XOUnitBuilder builder, StoreConfiguration storeConfiguration) {
        builder.provider(RemoteNeo4jXOProvider.class);
        Properties properties = new Properties();
        String username = this.storeConfiguration.getUsername();
        if (username != null) {
            properties.setProperty(Property.USERNAME.getKey(), username);
        }
        String password = this.storeConfiguration.getPassword();
        if (password != null) {
            properties.setProperty(Property.PASSWORD.getKey(), password);
        }
        String encryptionLevel = this.storeConfiguration.getEncryptionLevel();
        if (encryptionLevel != null) {
            properties.setProperty(Property.ENCRYPTION_LEVEL.getKey(), encryptionLevel);
        }
        Properties storeConfigurationProperties = this.storeConfiguration.getProperties();
        if (storeConfigurationProperties != null) {
            properties.putAll(storeConfigurationProperties);
        }
        builder.properties(properties);
        return builder.build();
    }

    @Override
    protected void initialize(XOManagerFactory xoManagerFactory) {
    }
}
