package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginInfo;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;

import org.assertj.core.api.Condition;
import org.jqassistant.schema.plugin.v2.JqassistantPlugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Verifies plugin repository related functionality.
 */
@ExtendWith(MockitoExtension.class)
class PluginRepositoryImplTest {

    @Mock
    private Scan scan;

    @Mock
    private Report report;

    /**
     * Verifies that properties are loaded and passed to plugins.
     */
    @Test
    void pluginProperties() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(
            new PluginClassLoader(PluginRepositoryImplTest.class.getClassLoader()));
        Map<String, String> properties = new HashMap<>();
        properties.put("testKey", "testValue");
        doReturn(properties).when(scan)
            .properties();
        doReturn(properties).when(report)
            .properties();
        PluginRepository pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        pluginRepository.initialize();
        // scanner plugins
        verifyProperties(getScannerPluginProperties(pluginRepository));
        // report plugins
        verifyProperties(getReportPluginProperties(pluginRepository));
        pluginRepository.destroy();
    }

    @Test
    void repositories() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(
            new PluginClassLoader(PluginRepositoryImplTest.class.getClassLoader()));
        PluginRepository pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        pluginRepository.initialize();
        // Scanner plugins
        ScannerContext scannerContext = mock(ScannerContext.class);
        Set<ScannerPlugin<?, ?>> scannerPlugins = pluginRepository.getScannerPluginRepository()
            .getScannerPlugins(scan, scannerContext);
        assertThat(scannerPlugins).hasSize(1)
            .haveExactly(1, new Condition<>() {
                @Override
                public boolean matches(ScannerPlugin<?, ?> value) {
                    return TestScannerPlugin.class.isAssignableFrom(value.getClass());
                }
            });
        // Report plugins
        ReportContext reportContext = mock(ReportContext.class);
        Map<String, ReportPlugin> reportPlugins = pluginRepository.getAnalyzerPluginRepository()
            .getReportPlugins(report, reportContext);
        assertThat(reportPlugins).hasSize(3);
        assertThat(reportPlugins.get(TestReportPlugin.class.getSimpleName())).isNotNull();
        assertThat(reportPlugins.get("testReport")).isNotNull();
        assertThat(reportPlugins.get("xml")).isNotNull();
        pluginRepository.destroy();
    }

    @Test
    void allPluginsKnownToThePluginReaderFormThePluginOverview() {
        PluginConfigurationReader pluginConfigurationReader = Mockito.mock(PluginConfigurationReader.class);

        JqassistantPlugin pluginA = Mockito.mock(JqassistantPlugin.class);
        JqassistantPlugin pluginB = Mockito.mock(JqassistantPlugin.class);
        JqassistantPlugin pluginC = Mockito.mock(JqassistantPlugin.class);

        doReturn("jqa.a").when(pluginA)
            .getId();
        doReturn("A").when(pluginA)
            .getName();
        doReturn("1.0.0").when(pluginA)
            .getVersion();
        doReturn("jqa.b").when(pluginB)
            .getId();
        doReturn("B").when(pluginB)
            .getName();
        doReturn("jqa.c").when(pluginC)
            .getId();
        doReturn("C").when(pluginC)
            .getName();

        doReturn(Arrays.asList(pluginA, pluginB, pluginC)).when(pluginConfigurationReader)
            .getPlugins();
        doReturn(PluginRepositoryImplTest.class.getClassLoader()).when(pluginConfigurationReader)
            .getClassLoader();

        PluginRepository pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        pluginRepository.initialize();

        Collection<PluginInfo> overview = pluginRepository.getPluginInfos();

        assertThat(overview).hasSize(3);
        assertThat(overview).anyMatch(info -> info.getName()
            .equals("A") && info.getId()
            .equals("jqa.a") && info.getVersion()
            .get()
            .equals("1.0.0"));
        assertThat(overview).anyMatch(info -> info.getName()
            .equals("B") && info.getId()
            .equals("jqa.b") && info.getVersion()
            .isEmpty());
        assertThat(overview).anyMatch(info -> info.getName()
            .equals("C") && info.getId()
            .equals("jqa.c") && info.getVersion()
            .isEmpty());
    }

    private void verifyProperties(Map<String, Object> pluginProperties) {
        assertThat(pluginProperties).isNotNull();
        assertThat(pluginProperties).containsEntry("testKey", "testValue");
    }

    private Map<String, Object> getScannerPluginProperties(PluginRepository pluginRepository) {
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        ScannerContext scannerContext = mock(ScannerContext.class);
        Set<ScannerPlugin<?, ?>> scannerPlugins = scannerPluginRepository.getScannerPlugins(scan, scannerContext);
        assertThat(scannerPlugins).isNotEmpty();
        for (ScannerPlugin<?, ?> scannerPlugin : scannerPlugins) {
            if (scannerPlugin instanceof TestScannerPlugin) {
                return ((TestScannerPlugin) scannerPlugin).getProperties();
            }
        }
        return null;
    }

    private Map<String, Object> getReportPluginProperties(PluginRepository pluginRepository) {
        AnalyzerPluginRepository analyzerPluginRepository = pluginRepository.getAnalyzerPluginRepository();
        Map<String, ReportPlugin> reportPlugins = analyzerPluginRepository.getReportPlugins(report, mock(ReportContext.class));
        assertThat(reportPlugins).isNotEmpty();
        for (ReportPlugin reportPlugin : reportPlugins.values()) {
            if (reportPlugin instanceof TestReportPlugin) {
                return ((TestReportPlugin) reportPlugin).getProperties();
            }
        }
        return null;
    }
}
