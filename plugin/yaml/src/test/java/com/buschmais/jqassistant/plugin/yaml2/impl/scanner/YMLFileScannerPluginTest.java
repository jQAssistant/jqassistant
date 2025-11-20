package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class YMLFileScannerPluginTest {

    @Nested
    class DefaultItemAcceptance {
        private final YMLFileScannerPlugin plugin = new YMLFileScannerPlugin();

        @Test
        void acceptsYAML() {
            assertThat(plugin.accepts(mock(FileResource.class), "/test.yaml", DefaultScope.NONE)).isTrue();
        }

        @Test
        void acceptsYML() {
            assertThat(plugin.accepts(mock(FileResource.class), "/test.yml", DefaultScope.NONE)).isTrue();
        }

        @Test
        void doesNotAcceptXML() {
            assertThat(plugin.accepts(mock(FileResource.class), "/test.xml", DefaultScope.NONE)).isFalse();
        }
    }

    @Nested
    class FilteredItemAcceptance {
        private final YMLFileScannerPlugin plugin = new YMLFileScannerPlugin();

        @BeforeEach
        void configure() {
            plugin.configure("*.yaml,*.yml,/path/*", "/path/excl.yaml");
        }

        @Test
        void acceptsYML() {
            assertThat(plugin.accepts(mock(FileResource.class), "/test.yml", DefaultScope.NONE)).isTrue();
        }

        @Test
        void acceptsPath() {
            assertThat(plugin.accepts(mock(FileResource.class), "/path/xxx", DefaultScope.NONE)).isTrue();
        }

        @Test
        void doesNotAcceptUnknownPath() {
            assertThat(plugin.accepts(mock(FileResource.class), "/other/abc", DefaultScope.NONE)).isFalse();
        }

        @Test
        void doesNotAcceptExcludedFile() {
            assertThat(plugin.accepts(mock(FileResource.class), "/path/excl.yaml", DefaultScope.NONE)).isFalse();
        }
    }
}
