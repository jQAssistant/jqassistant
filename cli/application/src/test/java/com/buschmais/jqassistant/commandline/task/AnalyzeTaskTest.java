package com.buschmais.jqassistant.commandline.task;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;

import org.apache.commons.cli.CommandLine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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
    private RuleSourceReaderPluginRepository ruleSourceReaderPluginRepository;

    @Mock
    private RuleLanguagePluginRepository ruleLanguagePluginRepository;

    @Before
    public void before() {
        when(pluginRepository.getClassLoader()).thenReturn(AnalyzeTaskTest.class.getClassLoader());
        when(pluginRepository.getModelPluginRepository()).thenReturn(modelPluginRepository);
        when(pluginRepository.getReportPluginRepository()).thenReturn(reportPluginRepository);
        when(pluginRepository.getRulePluginRepository()).thenReturn(rulePluginRepository);
        when(pluginRepository.getRuleSourceReaderPluginRepository()).thenReturn(ruleSourceReaderPluginRepository);
        when(pluginRepository.getRuleLanguagePluginRepository()).thenReturn(ruleLanguagePluginRepository);
    }

    @Test
    public void loadPlugins() throws CliExecutionException, PluginRepositoryException, RuleException {
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
        Assert.assertThat(propertiesCaptor.getValue(), is(pluginProperties));
        verify(rulePluginRepository).getRuleSources();
        verify(modelPluginRepository).getDescriptorTypes();
        verify(ruleSourceReaderPluginRepository).getRuleSourceReaderPlugins(any(RuleConfiguration.class));
        verify(ruleLanguagePluginRepository).getRuleLanguagePlugins(anyMap());
    }

    private void stubOption(CommandLine standardOptions, String option, String value) {
        when(standardOptions.hasOption(option)).thenReturn(true);
        when(standardOptions.getOptionValue(option)).thenReturn(value);
    }
}

