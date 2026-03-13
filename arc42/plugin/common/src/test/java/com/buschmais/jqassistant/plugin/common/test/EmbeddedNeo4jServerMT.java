package com.buschmais.jqassistant.plugin.common.test;

import java.io.IOException;

import com.buschmais.jqassistant.core.test.plugin.AbstractServerPluginIT;

import org.junit.jupiter.api.Test;

/**
 * Manual test to start a server.
 */
class EmbeddedNeo4jServerMT extends AbstractServerPluginIT {

    @Test
    void server() throws IOException {
        System.out.println("Hit Enter to continue.");
        System.in.read();
    }
}
