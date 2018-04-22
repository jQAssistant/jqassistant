package com.buschmais.jqassistant.commandline.test;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.task.AnalyzeTask;
import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.report.api.ReportContext;

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
    private RuleLanguagePluginRepository ruleLanguagePluginRepository;

    @Before
    public void before() throws PluginRepositoryException {
        when(pluginRepository.getClassLoader()).thenReturn(AnalyzeTaskTest.class.getClassLoader());
        when(pluginRepository.getModelPluginRepository()).thenReturn(modelPluginRepository);
        when(pluginRepository.getReportPluginRepository()).thenReturn(reportPluginRepository);
        when(pluginRepository.getRulePluginRepository()).thenReturn(rulePluginRepository);
        when(pluginRepository.getRuleLanguagePluginRepository()).thenReturn(ruleLanguagePluginRepository);
    }

    @Test
    public void loadPlugins() throws CliExecutionException, PluginRepositoryException {
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
    }

    private void stubOption(CommandLine standardOptions, String option, String value) {
        when(standardOptions.hasOption(option)).thenReturn(true);
        when(standardOptions.getOptionValue(option)).thenReturn(value);
    }
}
