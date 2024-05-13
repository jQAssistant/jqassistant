package com.buschmais.jqassistant.commandline.task;

import java.net.URI;
import java.net.URISyntaxException;

import com.buschmais.jqassistant.commandline.CliExecutionException;
import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.spi.RulePluginRepository;
import com.buschmais.jqassistant.core.runtime.api.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.store.api.configuration.Embedded;
import com.buschmais.jqassistant.core.store.api.configuration.Remote;
import com.buschmais.jqassistant.core.store.api.configuration.Store;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static java.util.Optional.of;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AnalyzeTaskTest {

    @Mock
    private CliConfiguration configuration;

    @Mock
    private Store store;

    @Mock
    private Embedded embedded;

    @Mock
    private Remote remote;

    @Mock
    private Analyze analyze;

    @Mock
    private Rule rule;

    @Mock
    private Report report;

    @Mock
    private PluginRepository pluginRepository;

    @Mock
    private ArtifactProvider artifactProvider;

    @Mock
    private StorePluginRepository storePluginRepository;

    @Mock
    private AnalyzerPluginRepository analyzerPluginRepository;

    @Mock
    private RulePluginRepository rulePluginRepository;

    @BeforeEach
    void before() throws URISyntaxException {
        doReturn(store).when(configuration)
            .store();
        doReturn(embedded).when(store)
            .embedded();
        doReturn(remote).when(store)
            .remote();
        doReturn(analyze).when(configuration)
            .analyze();
        doReturn(report).when(analyze)
            .report();
        doReturn(rule).when(analyze)
            .rule();
        doReturn(of(new URI("memory:///"))).when(store)
            .uri();
        doReturn(Severity.MINOR.name()).when(report)
            .warnOnSeverity();
        doReturn(Severity.MAJOR.name()).when(report)
            .failOnSeverity();
        when(pluginRepository.getClassLoader()).thenReturn(AnalyzeTaskTest.class.getClassLoader());
        when(pluginRepository.getStorePluginRepository()).thenReturn(storePluginRepository);
        when(pluginRepository.getAnalyzerPluginRepository()).thenReturn(analyzerPluginRepository);
        when(pluginRepository.getRulePluginRepository()).thenReturn(rulePluginRepository);
    }

    @Test
    void loadPlugins() throws CliExecutionException, RuleException {
        AnalyzeTask analyzeTask = new AnalyzeTask();
        analyzeTask.initialize(pluginRepository, artifactProvider);
        CommandLine options = mock(CommandLine.class);
        stubOption(options, "reportDirectory", "target/jqassistant/test/report");
        analyzeTask.configure(options, mock(ConfigurationBuilder.class));
        CommandLine standardOptions = mock(CommandLine.class);
        stubOption(standardOptions, "s", "target/jqassistant/test/store");

        analyzeTask.run(configuration, mock(Options.class));

        verify(analyzerPluginRepository).getReportPlugins(eq(report), any(ReportContext.class));
        verify(storePluginRepository).getDescriptorTypes();
        verify(storePluginRepository).getProcedureTypes();
        verify(storePluginRepository).getFunctionTypes();
        verify(rulePluginRepository).getRuleSources();
        verify(rulePluginRepository).getRuleParserPlugins(rule);
        verify(analyzerPluginRepository).getRuleInterpreterPlugins(anyMap());
    }

    private void stubOption(CommandLine standardOptions, String option, String value) {
        when(standardOptions.hasOption(option)).thenReturn(true);
        when(standardOptions.getOptionValue(option)).thenReturn(value);
    }
}
