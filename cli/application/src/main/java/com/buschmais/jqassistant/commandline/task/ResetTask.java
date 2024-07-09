package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;

import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class ResetTask extends AbstractStoreTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResetTask.class);

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    public void run(CliConfiguration configuration, Options options) throws CliExecutionException {
        withStore(configuration, store -> {
            LOGGER.info("Resetting store.");
            store.reset();
        });
    }
}
