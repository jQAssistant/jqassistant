package com.buschmais.jqassistant.commandline.task;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;

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
public class AnalyzeTaskTest {

    @Mock
    private PluginRepository pluginRepository;

    @Mock
    private ModelPluginRepository modelPluginRepository;

    @Mock
    private ReportPluginRepository reportPluginRepository;

    @Mock
    private RulePluginRepository rulePluginRepository;

    @Mock
    private RuleParserPluginRepository ruleParserPluginRepository;

    @Mock
    private RuleInterpreterPluginRepository ruleInterpreterPluginRepository;

    @BeforeEach
    public void before() {
        when(pluginRepository.getClassLoader()).thenReturn(AnalyzeTaskTest.class.getClassLoader());
        when(pluginRepository.getModelPluginRepository()).thenReturn(modelPluginRepository);
        when(pluginRepository.getReportPluginRepository()).thenReturn(reportPluginRepository);
        when(pluginRepository.getRulePluginRepository()).thenReturn(rulePluginRepository);
        when(pluginRepository.getRuleParserPluginRepository()).thenReturn(ruleParserPluginRepository);
        when(pluginRepository.getRuleInterpreterPluginRepository()).thenReturn(ruleInterpreterPluginRepository);
    }

    @Test
    public void loadPlugins() throws CliExecutionException, RuleException {
        AnalyzeTask analyzeTask = new AnalyzeTask();
        Map<String, Object> pluginProperties = new HashMap<>();
        analyzeTask.initialize(pluginRepository, pluginProperties);
        CommandLine options = mock(CommandLine.class);
        stubOption(options, "reportDirectory", "target/jqassistant/test/report");
        analyzeTask.withOptions(options);
        CommandLine standardOptions = mock(CommandLine.class);
        stubOption(standardOptions, "s", "target/jqassistant/test/store");
        analyzeTask.withStandardOptions(standardOptions);
        analyzeTask.run();

        ArgumentCaptor<Map> propertiesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(reportPluginRepository).getReportPlugins(any(ReportContext.class), propertiesCaptor.capture());
        assertThat(propertiesCaptor.getValue()).isSameAs(pluginProperties);
        verify(rulePluginRepository).getRuleSources();
        verify(modelPluginRepository).getDescriptorTypes();
        verify(ruleParserPluginRepository).getRuleParserPlugins(any(RuleConfiguration.class));
        verify(ruleInterpreterPluginRepository).getRuleInterpreterPlugins(anyMap());
        verify(modelPluginRepository).getProcedureTypes();
        verify(modelPluginRepository).getFunctionTypes();
    }

    private void stubOption(CommandLine standardOptions, String option, String value) {
        when(standardOptions.hasOption(option)).thenReturn(true);
        when(standardOptions.getOptionValue(option)).thenReturn(value);
    }
}

