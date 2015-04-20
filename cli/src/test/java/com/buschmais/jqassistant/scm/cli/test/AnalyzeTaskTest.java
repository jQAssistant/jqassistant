package com.buschmais.jqassistant.scm.cli.test;

import static org.mockito.Mockito.*;

import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.task.AnalyzeTask;

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

    @Before
    public void before() throws PluginRepositoryException {
        when(pluginRepository.getClassLoader()).thenReturn(AnalyzeTaskTest.class.getClassLoader());
        when(pluginRepository.getModelPluginRepository()).thenReturn(modelPluginRepository);
        when(pluginRepository.getReportPluginRepository()).thenReturn(reportPluginRepository);
        when(pluginRepository.getRulePluginRepository()).thenReturn(rulePluginRepository);
    }

    @Test
    public void loadPlugins() throws CliExecutionException, PluginRepositoryException {
        AnalyzeTask analyzeTask = new AnalyzeTask();
        HashMap<String, Object> pluginProperties = new HashMap<>();
        analyzeTask.initialize(pluginRepository, pluginProperties);
        CommandLine options = mock(CommandLine.class);
        stubOption(options, "reportDirectory", "target/jqassistant/test/report");
        analyzeTask.withOptions(options);
        CommandLine standardOptions = mock(CommandLine.class);
        stubOption(standardOptions, "s", "target/jqassistant/test/store");
        analyzeTask.withStandardOptions(standardOptions);
        analyzeTask.run();

        verify(reportPluginRepository).getReportPlugins(pluginProperties);
        verify(rulePluginRepository).getRuleSources();
        verify(modelPluginRepository).getDescriptorTypes();
    }

    private void stubOption(CommandLine standardOptions, String option, String value) {
        when(standardOptions.hasOption(option)).thenReturn(true);
        when(standardOptions.getOptionValue(option)).thenReturn(value);
    }
}
