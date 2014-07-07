package com.buschmais.jqassistant.scm.cli;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import java.io.IOException;
import java.util.Collections;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.neo4jserver.api.Server;
import com.buschmais.jqassistant.scm.neo4jserver.impl.DefaultServerImpl;

/**
 * @author jn4, Kontext E GmbH, 23.01.14
 */
public class CmdlineServer extends CommonJqAssistantTask {

    public CmdlineServer() {
        super("server");
    }

    protected void doTheTask(final Store store) {
        Server server = new DefaultServerImpl((EmbeddedGraphStore) store, getScannerPluginRepository(store, Collections.<String, Object> emptyMap()),
                getRulePluginRepository());
        server.start();
        getLog().info("Running server");
        getLog().info("Press <Enter> to finish.");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }

    public static void main(String[] args) {
        new CmdlineServer().run();
    }
}
