package com.buschmais.jqassistant.scm.cli;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import com.buschmais.jqassistant.core.store.api.Store;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class ResetTask extends AbstractJQATask {
    public ResetTask() {
        super("reset");
    }

    @Override
    protected void doTheTask(final Store store) {
        getLog().info("Resetting store.");
        store.reset();
    }
}
