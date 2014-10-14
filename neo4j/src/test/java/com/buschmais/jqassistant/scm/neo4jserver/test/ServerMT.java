package com.buschmais.jqassistant.scm.neo4jserver.test;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;

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
