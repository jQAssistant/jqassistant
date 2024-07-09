package com.buschmais.jqassistant.plugin.xml.test;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.xml.impl.scanner.XmlFileScannerPlugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class XmlFileScannerTest {

    @Mock
    private ScannerContext context;

    @Mock
    private FileResource fileResource;

    private XmlFileScannerPlugin scannerPlugin = new XmlFileScannerPlugin();

    @Test
    void noFilePattern() throws IOException {
        configure(null, null);
        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE)).isEqualTo(false);
        assertThat(scannerPlugin.accepts(fileResource, "test.xml", DefaultScope.NONE)).isEqualTo(true);
    }

    @Test
    void includeFilePattern() throws IOException {
        configure("*.xml", null);
        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE)).isEqualTo(false);
        assertThat(scannerPlugin.accepts(fileResource, "test.xml", DefaultScope.NONE)).isEqualTo(true);
    }

    @Test
    void includeAndExcludeFilePattern() throws IOException {
        configure("test.*", "*.xml");
        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE)).isEqualTo(true);
        assertThat(scannerPlugin.accepts(fileResource, "test.xml", DefaultScope.NONE)).isEqualTo(false);
    }

    private void configure(String includes, String excludes) {
        Map<String, Object> properties = MapBuilder.<String, Object>builder().entry(XmlFileScannerPlugin.PROPERTY_INCLUDE, includes)
            .entry(XmlFileScannerPlugin.PROPERTY_EXCLUDE, excludes).build();
        scannerPlugin.configure(context, properties);
    }
}
