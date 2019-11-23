package com.buschmais.jqassistant.scm.maven;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs analysis according to the defined rules.
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, configurator = "custom")
public class AnalyzeMojo extends AbstractProjectMojo {

    private Logger logger = LoggerFactory.getLogger(AnalyzeMojo.class);

    /**
     * The rule parameters to use (optional).
     */
    @Parameter(property = "jqassistant.ruleParameters")
    protected Map<String, String> ruleParameters;

    /**
     * If set also execute concepts which have already been applied.
     */
    @Parameter(property = "jqassistant.executeAppliedConcepts")
    protected boolean executeAppliedConcepts = false;

    /**
     * The severity threshold to warn on rule violations.
     */
    @Parameter(property = "jqassistant.warnOnSeverity")
    protected Severity warnOnSeverity = RuleConfiguration.DEFAULT.getDefaultConceptSeverity();

    /**
     * The severity threshold to fail on rule violations, i.e. break the build.
     */
    @Parameter(property = "jqassistant.failOnSeverity")
    protected Severity failOnSeverity = RuleConfiguration.DEFAULT.getDefaultConstraintSeverity();

    /**
     * Defines the set of reports which shall be created by default. If empty all
     * available reports will be used.
     */
    @Parameter(property = "jqassistant.reportTypes")
    private Set<String> reportTypes;

    @Parameter(property = "jqassistant.reportProperties")
    private Map<String, Object> reportProperties;

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing analysis for '" + rootModule.getName() + "'.");
        getLog().info("Will warn on violations starting from severity '" + warnOnSeverity + "'");
        getLog().info("Will fail on violations starting from severity '" + failOnSeverity + "'.");

        RuleSet ruleSet = readRules(rootModule);
        RuleSelection ruleSelection = RuleSelection.select(ruleSet, groups, constraints, concepts);
        ReportContext reportContext = new ReportContextImpl(ProjectResolver.getOutputDirectory(rootModule));
        Severity effectiveFailOnSeverity = failOnSeverity;
        Map<String, Object> properties = getReportProperties();
        Map<String, ReportPlugin> reportPlugins = pluginRepositoryProvider.getPluginRepository().getAnalyzerPluginRepository().getReportPlugins(reportContext, properties);
        InMemoryReportPlugin inMemoryReportPlugin = new InMemoryReportPlugin(
                new CompositeReportPlugin(reportPlugins, reportTypes.isEmpty() ? null : reportTypes));
        AnalyzerConfiguration configuration = new AnalyzerConfiguration();
        configuration.setExecuteAppliedConcepts(executeAppliedConcepts);
        try {
            Analyzer analyzer = new AnalyzerImpl(configuration, store, pluginRepositoryProvider.getPluginRepository().getAnalyzerPluginRepository().getRuleInterpreterPlugins(Collections.<String, Object>emptyMap()), inMemoryReportPlugin, logger);
            analyzer.execute(ruleSet, ruleSelection, ruleParameters);
        } catch (RuleException e) {
            throw new MojoExecutionException("Analysis failed.", e);
        }
        ReportHelper reportHelper = new ReportHelper(logger);
        store.beginTransaction();
        try {
            verifyAnalysisResults(inMemoryReportPlugin, reportHelper, effectiveFailOnSeverity);
        } finally {
            store.commitTransaction();
        }
    }

    private Map<String, Object> getReportProperties() {
        Map<String, Object> properties = reportProperties != null ? reportProperties : new HashMap<>();
        if (xmlReportFile != null) {
            properties.put(XmlReportPlugin.XML_REPORT_FILE, xmlReportFile.getAbsolutePath());
        }
        return properties;
    }

    private void verifyAnalysisResults(InMemoryReportPlugin inMemoryReportWriter, ReportHelper reportHelper, Severity effectiveFailOnSeverity)
            throws MojoFailureException {
        int conceptViolations = reportHelper.verifyConceptResults(warnOnSeverity, effectiveFailOnSeverity, inMemoryReportWriter);
        int constraintViolations = reportHelper.verifyConstraintResults(warnOnSeverity, effectiveFailOnSeverity, inMemoryReportWriter);

        boolean hasConceptViolations = conceptViolations > 0;
        boolean hasConstraintViolations = constraintViolations > 0;
        boolean hasViolations = hasConceptViolations || hasConstraintViolations;

        if (hasViolations) {
            throw new MojoFailureException("Violations detected: " + conceptViolations + " concepts, " + constraintViolations + " constraints");
        }
    }
}
