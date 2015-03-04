package com.buschmais.jqassistant.scm.cli.test;

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
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.PluginRepository;
import com.buschmais.jqassistant.scm.cli.test.plugin.TestReportPlugin;
import com.buschmais.jqassistant.scm.cli.test.plugin.TestScannerPlugin;

/**
 * Verifies plugin repository related functionality.
 */
public class PluginRepositoryTest {

    /**
     * Verifies that properties are loaded and passed to plugins.
     * 
     * @throws com.buschmais.jqassistant.scm.cli.CliExecutionException
     *             If the test fails.
     */
    @Test
    public void pluginProperties() throws CliExecutionException, PluginRepositoryException {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(PluginRepositoryTest.class.getClassLoader());
        Map<String, Object> properties = new HashMap<>();
        properties.put("testKey", "testValue");
        PluginRepository pluginRepository = new PluginRepository(pluginConfigurationReader, properties);
        // scanner plugins
        verifyProperties(getScannerPluginProperties(pluginRepository));
        // report plugins
        verifyProperties(getReportPluginProperties(pluginRepository));
    }

    private void verifyProperties(Map<String, Object> pluginProperties) {
        assertThat(pluginProperties, notNullValue());
        assertThat(pluginProperties.get("testKey"), CoreMatchers.<Object> equalTo("testValue"));
    }

    private Map<String, Object> getScannerPluginProperties(PluginRepository pluginRepository) throws PluginRepositoryException {
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        List<ScannerPlugin<?, ?>> scannerPlugins = scannerPluginRepository.getScannerPlugins();
        assertThat(scannerPlugins.size(), greaterThan(0));
        for (ScannerPlugin<?, ?> scannerPlugin : scannerPlugins) {
            if (scannerPlugin instanceof TestScannerPlugin) {
                return ((TestScannerPlugin) scannerPlugin).getProperties();
            }
        }
        return null;
    }

    private Map<String, Object> getReportPluginProperties(PluginRepository pluginRepository) throws PluginRepositoryException {
        ReportPluginRepository reportPluginRepository = pluginRepository.getReportPluginRepository();
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
