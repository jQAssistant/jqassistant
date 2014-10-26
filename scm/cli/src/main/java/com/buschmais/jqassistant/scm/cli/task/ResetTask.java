package com.buschmais.jqassistant.scm.cli.task;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import org.apache.commons.cli.CommandLine;

import com.buschmais.jqassistant.core.store.api.Store;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class ResetTask extends AbstractJQATask {

    @Override
    protected void executeTask(final Store store) {
        getLog().info("Resetting store.");
        store.reset();
    }

    @Override
    public void withOptions(CommandLine options) {
    }
}
