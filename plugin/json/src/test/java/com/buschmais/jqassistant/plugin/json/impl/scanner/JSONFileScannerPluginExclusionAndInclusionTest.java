package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JSONFileScannerPluginExclusionAndInclusionTest {

    @Mock
    private ScannerContext context;

    @Mock
    private FileResource fileResource;

    private JSONFileScannerPlugin scannerPlugin = new JSONFileScannerPlugin();

    @Test
    void noFilePattern() throws IOException {
        configure(null, null);

        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE)).isFalse();
        assertThat(scannerPlugin.accepts(fileResource, "test.json", DefaultScope.NONE)).isTrue();
    }

    @Test
    void includeFilePattern() throws IOException {
        configure("*.json", null);

        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE)).isFalse();
        assertThat(scannerPlugin.accepts(fileResource, "test.json", DefaultScope.NONE)).isTrue();
    }

    @Test
    void includeAndExcludeFilePattern() throws IOException {
        configure("test.*", "*.json");

        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE)).isTrue();
        assertThat(scannerPlugin.accepts(fileResource, "test.json", DefaultScope.NONE)).isFalse();
    }

    @Test
    void includeWithDirectoryPart() throws Exception {
        configure("*/data/test.*", null);

        assertThat(scannerPlugin.accepts(fileResource, "test/data/test.txt", DefaultScope.NONE)).isTrue();
        assertThat(scannerPlugin.accepts(fileResource, "/test/test.json", DefaultScope.NONE)).isFalse();
    }

    @Test
    void excludeWithDirectoryPart() throws Exception {
        configure(null, "*/data/test.*");

        assertThat(scannerPlugin.accepts(fileResource, "test/data/test.txt", DefaultScope.NONE)).isFalse();
        assertThat(scannerPlugin.accepts(fileResource, "/test/test.json", DefaultScope.NONE)).isTrue();
    }

    private void configure(String inclusionPattern, String exclusionPattern) {
        Map<String, Object> properties =
            MapBuilder.<String, Object>builder().entry(JSONFileScannerPlugin.PROPERTY_INCLUDE, inclusionPattern)
                .entry(JSONFileScannerPlugin.PROPERTY_EXCLUDE, exclusionPattern).build();

        scannerPlugin.configure(context, properties);
    }
}
