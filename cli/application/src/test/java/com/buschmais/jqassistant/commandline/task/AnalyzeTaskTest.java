package com.buschmais.jqassistant.commandline.task;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.PropertiesConfigBuilder;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.spi.RulePluginRepository;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AnalyzeTaskTest {

    @Mock
    private Configuration configuration;

    @Mock
    private PluginRepository pluginRepository;

    @Mock
    private StorePluginRepository storePluginRepository;

    @Mock
    private AnalyzerPluginRepository analyzerPluginRepository;

    @Mock
    private RulePluginRepository rulePluginRepository;

    @BeforeEach
    void before() {
        when(pluginRepository.getClassLoader()).thenReturn(AnalyzeTaskTest.class.getClassLoader());
        when(pluginRepository.getStorePluginRepository()).thenReturn(storePluginRepository);
        when(pluginRepository.getAnalyzerPluginRepository()).thenReturn(analyzerPluginRepository);
        when(pluginRepository.getRulePluginRepository()).thenReturn(rulePluginRepository);
    }

    @Test
    void loadPlugins() throws CliExecutionException, RuleException {
        AnalyzeTask analyzeTask = new AnalyzeTask();
        Map<String, Object> pluginProperties = new HashMap<>();
        analyzeTask.initialize(pluginRepository, pluginProperties);
        CommandLine options = mock(CommandLine.class);
        stubOption(options, "reportDirectory", "target/jqassistant/test/report");
        analyzeTask.withOptions(options, mock(PropertiesConfigBuilder.class));
        CommandLine standardOptions = mock(CommandLine.class);
        stubOption(standardOptions, "s", "target/jqassistant/test/store");
        analyzeTask.withStandardOptions(standardOptions, mock(PropertiesConfigBuilder.class));
        analyzeTask.run(configuration);

        ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(analyzerPluginRepository).getReportPlugins(any(ReportContext.class), propertiesCaptor.capture());
        assertThat(propertiesCaptor.getValue()).isSameAs(pluginProperties);
        verify(storePluginRepository).getDescriptorTypes();
        verify(storePluginRepository).getProcedureTypes();
        verify(storePluginRepository).getFunctionTypes();
        verify(rulePluginRepository).getRuleSources();
        verify(rulePluginRepository).getRuleParserPlugins(any(RuleConfiguration.class));
        verify(analyzerPluginRepository).getRuleInterpreterPlugins(anyMap());
    }

    private void stubOption(CommandLine standardOptions, String option, String value) {
        when(standardOptions.hasOption(option)).thenReturn(true);
        when(standardOptions.getOptionValue(option)).thenReturn(value);
    }
}
