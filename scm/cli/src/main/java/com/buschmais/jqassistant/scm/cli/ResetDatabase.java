package com.buschmais.jqassistant.scm.cli;

import com.buschmais.jqassistant.core.store.api.Store;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;


/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class ResetDatabase extends CommonJqAssistantTask {
    public ResetDatabase() {
        super("reset");
    }

    @Override
    protected void doTheTask(final Store store) {
        getLog().info("Resetting store.");
        store.reset();
    }
}
