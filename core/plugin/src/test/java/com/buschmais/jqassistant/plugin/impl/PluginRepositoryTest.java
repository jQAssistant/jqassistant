package com.buschmais.jqassistant.plugin.impl;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ReportPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.plugin.impl.plugin.TestReportPlugin;
import com.buschmais.jqassistant.plugin.impl.plugin.TestScannerPlugin;

/**
 * Verifies plugin repository related functionality.
 */
public class PluginRepositoryTest {

    /**
     * Verifies that properties are loaded and passed to plugins.
     * 
     * @throws PluginRepositoryException
     *             If the test fails.
     */
    @Test
    public void pluginProperties() throws PluginRepositoryException {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(PluginRepositoryTest.class.getClassLoader());
        Map<String, Object> properties = new HashMap<>();
        properties.put("testKey", "testValue");
        com.buschmais.jqassistant.core.plugin.api.PluginRepository pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        // scanner plugins
        verifyProperties(getScannerPluginProperties(pluginRepository, properties));
        // report plugins
        verifyProperties(getReportPluginProperties(pluginRepository, properties));
    }

    private void verifyProperties(Map<String, Object> pluginProperties) {
        assertThat(pluginProperties, notNullValue());
        assertThat(pluginProperties.get("testKey"), CoreMatchers.<Object> equalTo("testValue"));
    }

    private Map<String, Object> getScannerPluginProperties(com.buschmais.jqassistant.core.plugin.api.PluginRepository pluginRepository, Map<String, Object> properties) throws PluginRepositoryException {
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository(properties);
        List<ScannerPlugin<?, ?>> scannerPlugins = scannerPluginRepository.getScannerPlugins();
        assertThat(scannerPlugins.size(), greaterThan(0));
        for (ScannerPlugin<?, ?> scannerPlugin : scannerPlugins) {
            if (scannerPlugin instanceof TestScannerPlugin) {
                return ((TestScannerPlugin) scannerPlugin).getProperties();
            }
        }
        return null;
    }

    private Map<String, Object> getReportPluginProperties(com.buschmais.jqassistant.core.plugin.api.PluginRepository pluginRepository, Map<String, Object> properties) throws PluginRepositoryException {
        ReportPluginRepository reportPluginRepository = pluginRepository.getReportPluginRepository(properties);
        List<ReportPlugin> reportPlugins = reportPluginRepository.getReportPlugins();
        assertThat(reportPlugins.size(), greaterThan(0));
        for (ReportPlugin reportPlugin : reportPlugins) {
            if (reportPlugin instanceof TestReportPlugin) {
                return ((TestReportPlugin) reportPlugin).getProperties();
            }
        }
        return null;
    }
}
