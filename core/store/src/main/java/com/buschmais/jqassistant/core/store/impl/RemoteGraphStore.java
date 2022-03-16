package com.buschmais.jqassistant.core.store.impl;

import java.util.Properties;

import com.buschmais.jqassistant.core.store.api.configuration.Store;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider.Property;

public class RemoteGraphStore extends AbstractGraphStore {

    public RemoteGraphStore(Store configuration, StorePluginRepository storePluginRepository) {
        super(configuration, storePluginRepository);
    }

    @Override
    protected int getAutocommitThreshold() {
        return 1048;
    }

    @Override
    protected XOUnit configure(XOUnit.XOUnitBuilder builder, Store configuration) {
        builder.provider(RemoteNeo4jXOProvider.class);
        Properties properties = new Properties();
        this.configuration.username()
            .ifPresent(username -> properties.setProperty(Property.USERNAME.getKey(), username));
        this.configuration.password()
            .ifPresent(password -> properties.setProperty(Property.PASSWORD.getKey(), password));
        boolean encryption = this.configuration.encryption();
        properties.setProperty(Property.ENCRYPTION.getKey(), Boolean.toString(encryption));
        this.configuration.trustStrategy()
            .ifPresent(trustStrategy -> properties.setProperty(Property.TRUST_STRATEGY.getKey(), trustStrategy));
        this.configuration.trustCertificate()
            .ifPresent(trustCertificate -> properties.setProperty(Property.TRUST_CERTIFICATE.getKey(), trustCertificate));
        properties.putAll(this.configuration.properties());
        builder.properties(properties);
        return builder.build();
    }

    @Override
    protected void initialize(XOManagerFactory xoManagerFactory) {
    }
}
