package com.buschmais.jqassistant.neo4jserver.test;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import org.junit.Test;

import java.io.IOException;

/**
 * Manual test to start a server.
 */
public class ServerMT extends AbstractServerTest {

    @Test
    public void server() throws IOException, PluginRepositoryException {
        System.out.println("Hit Enter to continue.");
        System.in.read();
    }
}
