package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@EnabledIfSystemProperty(named = "jqassistant.yaml2.activate", matches = "^true$")
class YMLFileScannerPluginTest {

    @Nested
    class ItemAcceptance {
        private YMLFileScannerPlugin plugin = new YMLFileScannerPlugin();

        @Test
        void acceptsYAML() throws IOException {
            assertThat(plugin.accepts(mock(FileResource.class), "/test.yaml", DefaultScope.NONE)).isTrue();
        }

        @Test
        void acceptsYML() throws IOException {
            assertThat(plugin.accepts(mock(FileResource.class), "/test.yml", DefaultScope.NONE)).isTrue();
        }

        @Test
        void doesNotAcceptXML() throws IOException {
            assertThat(plugin.accepts(mock(FileResource.class), "/test.xml", DefaultScope.NONE)).isFalse();
        }
    }
}
