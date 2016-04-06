package com.buschmais.jqassistant.plugin.impl;

import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.plugin.impl.plugin.TestReportPlugin;
import com.buschmais.jqassistant.plugin.impl.plugin.TestScannerPlugin;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies plugin repository related functionality.
 */
public class PluginRepositoryTest {

    /**
     * Verifies that properties are loaded and passed to plugins.
     *
     * @throws PluginRepositoryException If the test fails.
     */
    @Test
    public void pluginProperties() throws PluginRepositoryException {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(PluginRepositoryTest.class.getClassLoader());
        Map<String, Object> properties = new HashMap<>();
        properties.put("testKey", "testValue");
        PluginRepository pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        // scanner plugins
        verifyProperties(getScannerPluginProperties(pluginRepository, properties));
        // report plugins
        verifyProperties(getReportPluginProperties(pluginRepository, properties));
    }

    @Test
    public void repositories() throws PluginRepositoryException {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(PluginRepositoryTest.class.getClassLoader());
        PluginRepository pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        // Scanner plugins
        ScannerContext scannerContext = mock(ScannerContext.class);
        Map<String, ScannerPlugin<?, ?>> scannerPlugins = pluginRepository.getScannerPluginRepository().getScannerPlugins(scannerContext, Collections.<String, Object>emptyMap());
        assertThat(scannerPlugins.size(), equalTo(2));
        assertThat(scannerPlugins.get(TestScannerPlugin.class.getSimpleName()), notNullValue());
        assertThat(scannerPlugins.get("testScanner"), notNullValue());
        // Report plugins
        Map<String, ReportPlugin> reportPlugins = pluginRepository.getReportPluginRepository().getReportPlugins(Collections.<String, Object>emptyMap());
        assertThat(reportPlugins.size(), equalTo(2));
        assertThat(reportPlugins.get(TestReportPlugin.class.getSimpleName()), notNullValue());
        assertThat(reportPlugins.get("testReport"), notNullValue());
    }

    private void verifyProperties(Map<String, Object> pluginProperties) {
        assertThat(pluginProperties, notNullValue());
        assertThat(pluginProperties.get("testKey"), CoreMatchers.<Object>equalTo("testValue"));
    }

    private Map<String, Object> getScannerPluginProperties(PluginRepository pluginRepository, Map<String, Object> properties) throws PluginRepositoryException {
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        ScannerContext scannerContext = mock(ScannerContext.class);
        Map<String, ScannerPlugin<?, ?>> scannerPlugins = scannerPluginRepository.getScannerPlugins(scannerContext, properties);
        assertThat(scannerPlugins.size(), greaterThan(0));
        for (ScannerPlugin<?, ?> scannerPlugin : scannerPlugins.values()) {
            if (scannerPlugin instanceof TestScannerPlugin) {
                return ((TestScannerPlugin) scannerPlugin).getProperties();
            }
        }
        return null;
    }

    private Map<String, Object> getReportPluginProperties(PluginRepository pluginRepository, Map<String, Object> properties) throws PluginRepositoryException {
        ReportPluginRepository reportPluginRepository = pluginRepository.getReportPluginRepository();
        Map<String, ReportPlugin> reportPlugins = reportPluginRepository.getReportPlugins(properties);
        assertThat(reportPlugins.size(), greaterThan(0));
        for (ReportPlugin reportPlugin : reportPlugins.values()) {
            if (reportPlugin instanceof TestReportPlugin) {
                return ((TestReportPlugin) reportPlugin).getProperties();
            }
        }
        return null;
    }
}
