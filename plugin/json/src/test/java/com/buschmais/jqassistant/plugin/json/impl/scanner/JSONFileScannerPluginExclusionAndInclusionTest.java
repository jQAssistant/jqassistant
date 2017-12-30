package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JSONFileScannerPluginExclusionAndInclusionTest {

    @Mock
    private ScannerContext context;

    @Mock
    private FileResource fileResource;

    private JSONFileScannerPlugin scannerPlugin = new JSONFileScannerPlugin();

    @Test
    public void noFilePattern() throws IOException {
        configure(null, null);

        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE)).isFalse();
        assertThat(scannerPlugin.accepts(fileResource, "test.json", DefaultScope.NONE)).isTrue();
    }

    @Test
    public void includeFilePattern() throws IOException {
        configure("*.json", null);

        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE)).isFalse();
        assertThat(scannerPlugin.accepts(fileResource, "test.json", DefaultScope.NONE)).isTrue();
    }

    @Test
    public void includeAndExcludeFilePattern() throws IOException {
        configure("test.*", "*.json");

        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE)).isTrue();
        assertThat(scannerPlugin.accepts(fileResource, "test.json", DefaultScope.NONE)).isFalse();
    }

    @Test
    public void includeWithDirectoryPart() throws Exception {
        configure("*/data/test.*", null);

        assertThat(scannerPlugin.accepts(fileResource, "test/data/test.txt", DefaultScope.NONE)).isTrue();
        assertThat(scannerPlugin.accepts(fileResource, "/test/test.json", DefaultScope.NONE)).isFalse();
    }

    @Test
    public void excludeWithDirectoryPart() throws Exception {
        configure(null, "*/data/test.*");

        assertThat(scannerPlugin.accepts(fileResource, "test/data/test.txt", DefaultScope.NONE)).isFalse();
        assertThat(scannerPlugin.accepts(fileResource, "/test/test.json", DefaultScope.NONE)).isTrue();
    }

    private void configure(String inclusionPattern, String exclusionPattern) {
        Map<String, Object> properties =
            MapBuilder.<String, Object>create(JSONFileScannerPlugin.PROPERTY_INCLUDE, inclusionPattern)
                .put(JSONFileScannerPlugin.PROPERTY_EXCLUDE, exclusionPattern).get();

        scannerPlugin.configure(context, properties);
    }
}
