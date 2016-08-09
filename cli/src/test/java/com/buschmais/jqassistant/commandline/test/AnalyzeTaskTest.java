package com.buschmais.jqassistant.commandline.test;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.task.AnalyzeTask;
import com.buschmais.jqassistant.core.plugin.api.ModelPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ReportPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import org.apache.commons.cli.CommandLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
