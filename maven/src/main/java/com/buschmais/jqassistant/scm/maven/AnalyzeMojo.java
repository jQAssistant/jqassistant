package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.configuration.api.PropertiesConfigBuilder;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.store.api.Store;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyMap;

/**
 * Runs analysis according to the defined rules.
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true, configurator = "custom")
public class AnalyzeMojo extends AbstractProjectMojo {

    public static final String JQASSISTANT_REPORT_CLASSIFIER = "jqassistant-report";

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
     * available default reports will be used.
     */
    @Parameter(property = "jqassistant.reportTypes")
    private Set<String> reportTypes;

    /**
     * Defines the properties for report plugins.
     */
    @Parameter(property = "jqassistant.reportProperties")
    private Map<String, Object> reportProperties;

    /**
     * If `true` a ZIP file `jqassistant-report.zip` containing the generated
     * reports is created in the folder `target/jqassistant` of the root module and
     * attached using the classifier `jqassistant-report`.
     */
    @Parameter(property = "jqassistant.attachReportArchive")
    private boolean attachReportArchive = false;

    @Component
    private MavenProjectHelper mavenProjectHelper;

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    protected void addConfigurationProperties(PropertiesConfigBuilder propertiesConfigBuilder) {
        propertiesConfigBuilder.with(Analyze.PREFIX, Analyze.EXECUTE_APPLIED_CONCEPTS, executeAppliedConcepts);
        propertiesConfigBuilder.with(Analyze.PREFIX, Analyze.RULE_PARAMETERS, ruleParameters);
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing analysis for '" + rootModule.getName() + "'.");
        getLog().info("Will warn on violations starting from severity '" + warnOnSeverity + "'");
        getLog().info("Will fail on violations starting from severity '" + failOnSeverity + "'.");

        RuleSet ruleSet = readRules(rootModule);
        RuleSelection ruleSelection = RuleSelection.select(ruleSet, groups, constraints, concepts);
        ReportContext reportContext = new ReportContextImpl(store, ProjectResolver.getOutputDirectory(rootModule));
        Severity effectiveFailOnSeverity = failOnSeverity;
        Map<String, Object> properties = getReportProperties();
        Map<String, ReportPlugin> reportPlugins = getPluginRepository().getAnalyzerPluginRepository().getReportPlugins(reportContext,
                properties);
        InMemoryReportPlugin inMemoryReportPlugin = new InMemoryReportPlugin(
                new CompositeReportPlugin(reportPlugins, reportTypes.isEmpty() ? null : reportTypes));
        Analyze analyze = getConfiguration().analyze();

        try {
            Analyzer analyzer = new AnalyzerImpl(analyze, store, getPluginRepository().getAnalyzerPluginRepository()
                    .getRuleInterpreterPlugins(emptyMap()), inMemoryReportPlugin, logger);
            analyzer.execute(ruleSet, ruleSelection);
        } catch (RuleException e) {
            throw new MojoExecutionException("Analysis failed.", e);
        }
        if (attachReportArchive) {
            attachReportArchive(rootModule, reportContext);
        }
        ReportHelper reportHelper = new ReportHelper(logger);
        store.beginTransaction();
        try {
            verifyAnalysisResults(inMemoryReportPlugin, reportHelper, effectiveFailOnSeverity);
        } finally {
            store.commitTransaction();
        }
    }

    private void attachReportArchive(MavenProject rootModule, ReportContext reportContext) throws MojoExecutionException {
        File reportArchive;
        try {
            reportArchive = reportContext.createReportArchive();
        } catch (ReportException e) {
            throw new MojoExecutionException("Cannot attach report artifact.", e);
        }
        logger.info("Created report archive {}.", reportArchive);
        mavenProjectHelper.attachArtifact(rootModule, "zip", JQASSISTANT_REPORT_CLASSIFIER, reportArchive);
        if (!currentProject.equals(rootModule)) {
            logger.info(
                    "Report archive has been attached to module '{}:{}:{}'. Use 'installAtEnd' (maven-install-plugin) or 'deployAtEnd' (maven-deploy-plugin) to ensure deployment to local or remote repositories.",
                    rootModule.getGroupId(), rootModule.getArtifactId(), rootModule.getVersion());
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
