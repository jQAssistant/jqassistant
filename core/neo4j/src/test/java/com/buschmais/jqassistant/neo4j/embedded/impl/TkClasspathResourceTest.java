package com.buschmais.jqassistant.neo4j.embedded.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TkClasspathResourceTest {

    @Test
    void resolveResource() {
        assertThat(TkClasspathResource.resolve("/"))
            .isEqualTo("browser/index.html");
        assertThat(TkClasspathResource.resolve("/index.html"))
            .isEqualTo("browser/index.html");
        assertThat(TkClasspathResource.resolve("/jqassistant/"))
            .isEqualTo("META-INF/jqassistant-static-content/index.html");
        assertThat(TkClasspathResource.resolve("/jqassistant/index.html"))
            .isEqualTo("META-INF/jqassistant-static-content/index.html");
    }

}
