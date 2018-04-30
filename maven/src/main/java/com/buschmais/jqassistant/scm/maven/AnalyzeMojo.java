package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.impl.report.JUnitReportPlugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;

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
     * Indicates if the plugin shall fail if a constraint violation is detected.
     */
    @Deprecated
    @Parameter(property = "jqassistant.failOnViolations")
    protected Boolean failOnViolations = null;

    /**
     * If set also execute concepts which have already been applied.
     */
    @Parameter(property = "jqassistant.executeAppliedConcepts")
    protected boolean executeAppliedConcepts = false;

    /**
     * Severity level for constraint violation failure check. Default value is
     * {@code Severity.INFO}
     */
    @Deprecated
    @Parameter(property = "jqassistant.severity")
    protected String severity;

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

    @Parameter(property = "jqassistant.junitReportDirectory")
    private java.io.File junitReportDirectory;

    /**
     * Defines the set of reports which shall be created by default. If empty all available reports will be used.
     */
    @Parameter(property = "jqassistant.reportTypes")
    private Set<String> reportTypes = new HashSet<>(asList(XmlReportPlugin.TYPE));

    @Parameter(property = "jqassistant.reportProperties")
    private Map<String, Object> reportProperties;

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing analysis for '" + rootModule.getName() + "'.");
        getLog().info("Will warn on violations starting form severity '" + warnOnSeverity + "'");
        getLog().info("Will fail on violations starting from severity '" + failOnSeverity + "'.");

        RuleSet ruleSet = readRules(rootModule);
        RuleSelection ruleSelection = RuleSelection.Builder.select(ruleSet, groups, constraints, concepts);
        ReportContext reportContext = new ReportContextImpl(ProjectResolver.getOutputDirectory(rootModule));
        Severity effectiveFailOnSeverity = getFailOnSeverity();
        Map<String, Object> properties = getReportProperties(rootModule, effectiveFailOnSeverity);
        Map<String, ReportPlugin> reportPlugins = getReportPlugins(reportContext, properties);
        InMemoryReportPlugin inMemoryReportPlugin = new InMemoryReportPlugin(new CompositeReportPlugin(reportPlugins, reportTypes));
        AnalyzerConfiguration configuration = new AnalyzerConfiguration();
        configuration.setExecuteAppliedConcepts(executeAppliedConcepts);
        try {
            Analyzer analyzer = new AnalyzerImpl(configuration, store, getRuleLanguagePlugins(), inMemoryReportPlugin, logger);
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

    private Severity getFailOnSeverity() throws MojoExecutionException {
        Severity effectiveFailOnSeverity;
        if (failOnViolations != null) {
            getLog().warn("The parameter 'failOnViolations' is deprecated, please use 'failOnSeverity' instead.");
        }
        if (severity != null) {
            getLog().warn("The parameter 'severity' is deprecated, please use 'failOnSeverity' instead.");
            try {
                effectiveFailOnSeverity = Severity.fromValue(severity);
            } catch (RuleException e) {
                throw new MojoExecutionException("Cannot evaluate parameter severity with value " + severity);
            }
        } else {
            effectiveFailOnSeverity = failOnSeverity;
        }
        return effectiveFailOnSeverity;
    }

    private Map<String, ReportPlugin> getReportPlugins(ReportContext reportContext, Map<String, Object> properties) throws MojoExecutionException {
        Map<String, ReportPlugin> reportPlugins;
        try {
            reportPlugins = pluginRepositoryProvider.getReportPluginRepository().getReportPlugins(reportContext, properties);
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot get report plugins.", e);
        }
        return reportPlugins;
    }

    private Map<String, Object> getReportProperties(MavenProject rootModule, Severity effectiveFailOnSeverity) {
        Map<String, Object> properties = reportProperties != null ? reportProperties : new HashMap<String, Object>();
        if (xmlReportFile != null) {
            properties.put(XmlReportPlugin.XML_REPORT_FILE, xmlReportFile.getAbsolutePath());
        }
        String junitReportDirectory = this.junitReportDirectory != null ? this.junitReportDirectory.getAbsolutePath()
                : new File(rootModule.getBuild().getDirectory() + "/surefire-reports").getAbsolutePath();
        properties.put(JUnitReportPlugin.JUNIT_REPORT_DIRECTORY, junitReportDirectory);
        properties.put(JUnitReportPlugin.JUNIT_ERROR_SEVERITY, effectiveFailOnSeverity.name());
        return properties;
    }

    private Map<String, Collection<RuleLanguagePlugin>> getRuleLanguagePlugins() throws MojoExecutionException {
        try {
            return pluginRepositoryProvider.getRuleLanguagePluginRepository().getRuleLanguagePlugins();
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot get rule language plugins.", e);
        }
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
