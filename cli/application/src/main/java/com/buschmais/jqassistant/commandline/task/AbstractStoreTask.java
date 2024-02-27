package com.buschmais.jqassistant.commandline.task;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.commandline.CliConfigurationException;
import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
import com.buschmais.jqassistant.core.store.api.configuration.Embedded;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public abstract class AbstractStoreTask extends AbstractTask {

    /**
     * Defines an operation to execute on an initialized store instance.
     */
    protected interface StoreOperation {
        /**
         * Execute the operation.
         *
         * @param store
         *     The store.
         * @throws CliExecutionException
         *     On execution errors.
         */
        void run(Store store) throws CliExecutionException;
    }

    /**
     * Execute a {@link StoreOperation}.
     *
     * @param configuration
     *     The {@link CliConfiguration}.
     * @param storeOperation
     *     The {@link StoreOperation}.
     * @throws CliExecutionException
     *     If the execution fails.
     */
    void withStore(CliConfiguration configuration, StoreOperation storeOperation) throws CliExecutionException {
        Store store = StoreFactory.getStore(configuration.store(), () -> new File(DEFAULT_STORE_DIRECTORY), pluginRepository.getStorePluginRepository(),
            artifactProvider);
        try {
            store.start();
            storeOperation.run(store);
        } finally {
            store.stop();
        }
    }

    @Override
    public void configure(CommandLine options, ConfigurationBuilder configurationBuilder) throws CliConfigurationException {
        configurationBuilder.with(Embedded.class, Embedded.CONNECTOR_ENABLED, isConnectorRequired());
    }

    @Override
    protected void addTaskOptions(List<Option> options) {
    }

    protected abstract boolean isConnectorRequired();

}
