package com.buschmais.jqassistant.core.store.impl;

import java.util.Properties;

import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider.Property;

public class RemoteGraphStore extends AbstractGraphStore {

    public RemoteGraphStore(StoreConfiguration configuration, StorePluginRepository storePluginRepository) {
        super(configuration, storePluginRepository);
    }

    @Override
    protected int getAutocommitThreshold() {
        return 1048;
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
        String encryption = this.storeConfiguration.getEncryption();
        if (encryption != null) {
            properties.setProperty(Property.ENCRYPTION.getKey(), encryption);
        }
        String trustStrategy = this.storeConfiguration.getTrustStrategy();
        if (trustStrategy != null) {
            properties.setProperty(Property.TRUST_STRATEGY.getKey(), trustStrategy);
        }
        String trustCertificate = this.storeConfiguration.getTrustCertificate();
        if (trustCertificate != null) {
            properties.setProperty(Property.TRUST_CERTIFICATE.getKey(), trustCertificate);
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
