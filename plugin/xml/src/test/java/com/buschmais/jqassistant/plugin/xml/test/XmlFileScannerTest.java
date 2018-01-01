package com.buschmais.jqassistant.plugin.xml.test;

import java.io.IOException;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.xml.impl.scanner.XmlFileScannerPlugin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class XmlFileScannerTest {

    @Mock
    private ScannerContext context;

    @Mock
    private FileResource fileResource;

    private XmlFileScannerPlugin scannerPlugin = new XmlFileScannerPlugin();

    @Test
    public void noFilePattern() throws IOException {
        configure(null, null);
        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE), equalTo(false));
        assertThat(scannerPlugin.accepts(fileResource, "test.xml", DefaultScope.NONE), equalTo(false));
    }

    @Test
    public void includeFilePattern() throws IOException {
        configure("*.xml", null);
        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE), equalTo(false));
        assertThat(scannerPlugin.accepts(fileResource, "test.xml", DefaultScope.NONE), equalTo(true));
    }

    @Test
    public void includeAndExcludeFilePattern() throws IOException {
        configure("test.*", "*.xml");
        assertThat(scannerPlugin.accepts(fileResource, "test.txt", DefaultScope.NONE), equalTo(true));
        assertThat(scannerPlugin.accepts(fileResource, "test.xml", DefaultScope.NONE), equalTo(false));
    }

    private void configure(String includes, String excludes) {
        Map<String, Object> properties = MapBuilder.<String, Object> create(XmlFileScannerPlugin.PROPERTY_INCLUDE, includes)
                .put(XmlFileScannerPlugin.PROPERTY_EXCLUDE, excludes).get();
        scannerPlugin.configure(context, properties);
    }
}
