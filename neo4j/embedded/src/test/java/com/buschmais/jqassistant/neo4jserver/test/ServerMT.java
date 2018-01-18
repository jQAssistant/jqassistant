package com.buschmais.jqassistant.neo4jserver.test;

import java.io.IOException;

import org.junit.Test;

/**
 * Manual test to start a server.
 */
public class ServerMT extends AbstractServerTest {

    @Test
    public void server() throws IOException {
        System.out.println("Hit Enter to continue.");
        System.in.read();
    }
}
