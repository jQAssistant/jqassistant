package com.buschmais.jqassistant.commandline.task;

import java.io.IOException;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class ServerTask extends AbstractStoreTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerTask.class);

    @Override
    protected boolean isConnectorRequired() {
        return true;
    }

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        withStore(configuration, store -> {
            EmbeddedGraphStore embeddedGraphStore = (EmbeddedGraphStore) store;
            EmbeddedNeo4jServer server = embeddedGraphStore.getEmbeddedNeo4jServer();
            LOGGER.info("Running server");
            server.start();
            if (configuration.server().daemon()) {
                // let the neo4j daemon do the job
                LOGGER.info("Running server. Use <Ctrl-C> to stop server.");
            } else {
                LOGGER.info("Press <Enter> to finish.");
                try {
                    System.in.read();
                } catch (IOException e) {
                    throw new CliExecutionException("Cannot read from console.", e);
                } finally {
                    server.stop();
                }
            }
            ;
        });
    }
}
