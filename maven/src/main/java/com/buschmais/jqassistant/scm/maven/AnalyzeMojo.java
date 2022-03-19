package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

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
public class AnalyzeMojo extends AbstractRuleMojo {

    public static final String JQASSISTANT_REPORT_CLASSIFIER = "jqassistant-report";

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeMojo.class);

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
    protected Severity warnOnSeverity;

    /**
     * The severity threshold to fail on rule violations, i.e. break the build.
     */
    @Parameter(property = "jqassistant.failOnSeverity")
    protected Severity failOnSeverity;

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

    /**
     * The file to write the XML report to.
     */
    @Parameter(property = "jqassistant.report.xml")
    private File xmlReportFile;

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
    protected void configure(ConfigurationBuilder configurationBuilder) throws MojoExecutionException {
        super.configure(configurationBuilder);
        configurationBuilder.with(Analyze.class, Analyze.EXECUTE_APPLIED_CONCEPTS, executeAppliedConcepts);
        configurationBuilder.with(Analyze.class, Analyze.RULE_PARAMETERS, ruleParameters);
        Map<String, Object> properties = reportProperties != null ? reportProperties : new HashMap<>();
        if (xmlReportFile != null) {
            properties.put(XmlReportPlugin.XML_REPORT_FILE, xmlReportFile.getAbsolutePath());
        }
        configurationBuilder.with(Report.class, Report.PROPERTIES, properties);
        configurationBuilder.with(Report.class, Report.WARN_ON_SEVERITY, warnOnSeverity);
        configurationBuilder.with(Report.class, Report.FAIL_ON_SEVERITY, failOnSeverity);
        configurationBuilder.with(Report.class, Report.CREATE_ARCHIVE, attachReportArchive);
    }

    @Override
    public void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException, MojoFailureException {
        MavenConfiguration configuration = mojoExecutionContext.getConfiguration();
        MavenProject rootModule = mojoExecutionContext.getRootModule();
        RuleSet ruleSet = readRules(mojoExecutionContext);
        RuleSelection ruleSelection = RuleSelection.select(ruleSet, groups, constraints, concepts);
        withStore(store -> analyze(configuration, rootModule, ruleSet, ruleSelection, store), mojoExecutionContext);
    }

    private void analyze(MavenConfiguration configuration, MavenProject rootModule, RuleSet ruleSet, RuleSelection ruleSelection, Store store)
        throws MojoExecutionException, MojoFailureException {
        Analyze analyze = configuration.analyze();
        getLog().info("Executing analysis for '" + rootModule.getName() + "'.");
        getLog().info("Will warn on violations starting from severity '" + analyze.report()
            .warnOnSeverity() + "'");
        getLog().info("Will fail on violations starting from severity '" + analyze.report()
            .failOnSeverity() + "'.");

        ReportContext reportContext = new ReportContextImpl(store, MojoExecutionContext.getOutputDirectory(rootModule));
        AnalyzerPluginRepository analyzerPluginRepository = getPluginRepository(configuration).getAnalyzerPluginRepository();
        Map<String, ReportPlugin> reportPlugins = analyzerPluginRepository.getReportPlugins(analyze.report(), reportContext);
        InMemoryReportPlugin inMemoryReportPlugin = new InMemoryReportPlugin(
            new CompositeReportPlugin(reportPlugins, reportTypes.isEmpty() ? null : reportTypes));

        try {
            Analyzer analyzer = new AnalyzerImpl(analyze, store, analyzerPluginRepository.getRuleInterpreterPlugins(emptyMap()), inMemoryReportPlugin, LOGGER);
            analyzer.execute(ruleSet, ruleSelection);
        } catch (RuleException e) {
            throw new MojoExecutionException("Analysis failed.", e);
        }
        if (analyze.report()
            .createArchive()) {
            attachReportArchive(rootModule, reportContext);
        }
        ReportHelper reportHelper = new ReportHelper(analyze.report(), LOGGER);
        store.beginTransaction();
        try {
            verifyAnalysisResults(inMemoryReportPlugin, reportHelper);
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
        LOGGER.info("Created report archive {}.", reportArchive);
        mavenProjectHelper.attachArtifact(rootModule, "zip", JQASSISTANT_REPORT_CLASSIFIER, reportArchive);
        if (!currentProject.equals(rootModule)) {
            LOGGER.info(
                "Report archive has been attached to module '{}:{}:{}'. Use 'installAtEnd' (maven-install-plugin) or 'deployAtEnd' (maven-deploy-plugin) to ensure deployment to local or remote repositories.",
                rootModule.getGroupId(), rootModule.getArtifactId(), rootModule.getVersion());
        }
    }

    private void verifyAnalysisResults(InMemoryReportPlugin inMemoryReportWriter, ReportHelper reportHelper) throws MojoFailureException {
        int conceptViolations = reportHelper.verifyConceptResults(inMemoryReportWriter);
        int constraintViolations = reportHelper.verifyConstraintResults(inMemoryReportWriter);

        boolean hasConceptViolations = conceptViolations > 0;
        boolean hasConstraintViolations = constraintViolations > 0;
        boolean hasViolations = hasConceptViolations || hasConstraintViolations;

        if (hasViolations) {
            throw new MojoFailureException("Violations detected: " + conceptViolations + " concepts, " + constraintViolations + " constraints");
        }
    }
}
