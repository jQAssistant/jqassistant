package com.buschmais.jqassistant.core.store.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Properties;

import org.neo4j.graphdb.GraphDatabaseService;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.XOManagerFactory;
import com.buschmais.xo.api.bootstrap.XO;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.remote.api.Neo4jRemoteStoreProvider;

public class RemoteGraphStore extends AbstractGraphStore {

    @Override
    protected GraphDatabaseService getGraphDatabaseService(XOManager xoManager) {
        throw new IllegalStateException("Not Supported.");
    }

    @Override
    protected XOManagerFactory createXOManagerFactory(Collection<Class<?>> types) {
        XOUnit xoUnit = null;
        Properties properties = new Properties();
        properties.setProperty("neo4j.remote.username", "neo4j");
        properties.setProperty("neo4j.remote.password", "admin");
        try {
            xoUnit = XOUnit.builder().uri(new URI("bolt://f1ws30:17687")).provider(Neo4jRemoteStoreProvider.class).types(types).properties(properties)
                    .mappingConfiguration(XOUnit.MappingConfiguration.builder().strictValidation(true).build()).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return XO.createXOManagerFactory(xoUnit);
    }

    @Override
    protected void closeXOManagerFactory(XOManagerFactory factory) {
        factory.close();
    }

    @Override
    protected int getAutocommitThreshold() {
        return 1024;
    }
}
