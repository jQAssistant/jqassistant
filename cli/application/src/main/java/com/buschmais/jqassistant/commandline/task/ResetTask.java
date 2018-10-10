package com.buschmais.jqassistant.commandline.task;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.commons.cli.Option;
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
    protected void addTaskOptions(List<Option> options) {
    }

    @Override
    protected void executeTask(final Store store) {
        LOGGER.info("Resetting store.");
        store.reset();
    }
}
