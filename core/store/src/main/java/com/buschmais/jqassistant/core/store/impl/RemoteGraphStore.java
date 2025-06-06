package com.buschmais.jqassistant.core.store.impl;

import java.net.URI;
import java.util.Properties;

import com.buschmais.jqassistant.core.store.api.configuration.Remote;
import com.buschmais.jqassistant.core.store.api.configuration.Store;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider;
import com.buschmais.xo.neo4j.remote.api.RemoteNeo4jXOProvider.Property;

public class RemoteGraphStore extends AbstractGraphStore {

    public RemoteGraphStore(URI uri, Store configuration, StorePluginRepository storePluginRepository) {
        super(uri, configuration, storePluginRepository);
    }

    @Override
    protected int getDefaultAutocommitThreshold() {
        return 1048;
    }

    @Override
    protected void configure(XOUnit.XOUnitBuilder builder) {
        builder.provider(RemoteNeo4jXOProvider.class);
        Properties properties = new Properties();
        Remote remote = this.configuration.remote();
        remote.username()
            .ifPresent(username -> properties.setProperty(Property.USERNAME.getKey(), username));
        remote.password()
            .ifPresent(password -> properties.setProperty(Property.PASSWORD.getKey(), password));
        boolean encryption = remote.encryption();
        properties.setProperty(Property.ENCRYPTION.getKey(), Boolean.toString(encryption));
        remote.trustStrategy()
            .ifPresent(trustStrategy -> properties.setProperty(Property.TRUST_STRATEGY.getKey(), trustStrategy));
        remote.trustCertificate()
            .ifPresent(trustCertificate -> properties.setProperty(Property.TRUST_CERTIFICATE.getKey(), trustCertificate));
        properties.putAll(remote.properties());
        builder.properties(properties);
    }

    @Override
    protected void initialize(XOManagerFactory<?, ?, ?, ?> xoManagerFactory) {
        // unused
    }

    @Override
    protected void destroy() {
        // unused
    }
}
