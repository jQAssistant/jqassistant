package com.buschmais.jqassistant.scm.neo4jserver.test;

import java.io.IOException;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

public abstract class AbstractDatabaseIT {


    protected void reset() throws IOException {
        /*FileUtils.deleteDirectory(new File(STORE_DIR));*/
    }

    protected EmbeddedGraphStore createStore() {
        return new EmbeddedGraphStore(getStoreDir());
    }

    protected abstract String getStoreDir();

}
