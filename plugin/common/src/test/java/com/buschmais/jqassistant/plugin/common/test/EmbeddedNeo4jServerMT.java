package com.buschmais.jqassistant.plugin.common.test;

import java.io.IOException;

import org.junit.Test;

/**
 * Manual test to start a server.
 */
public class EmbeddedNeo4jServerMT extends AbstractEmbeddedNeo4jServerIT {

    @Test
    public void server() throws IOException {
        System.out.println("Hit Enter to continue.");
        System.in.read();
    }
}
