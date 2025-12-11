package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.baseline.BaselineManager;
import com.buschmais.jqassistant.core.analysis.api.baseline.BaselineRepository;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.api.configuration.Baseline;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.RuleHelper;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyMap;

/**
 * Runs analysis according to the defined rules.
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY, threadSafe = true)
public class AnalyzeMojo extends AbstractMojo {

    public static final String JQASSISTANT_REPORT_CLASSIFIER = "jqassistant-report";

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeMojo.class);

    @Component
    private MavenProjectHelper mavenProjectHelper;

    @Override
    protected MavenTask getMavenTask() {
        return new AbstractMavenRuleTask(cachingStoreProvider) {

            @Override
            public void enterProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException {
                MavenProject rootModule = mavenTaskContext.getRootModule();
                List<MavenProject> projectModules = mavenTaskContext.getProjects()
                    .get(rootModule);
                if (projectModules.size() > 1) {
                    validate(mavenTaskContext);
                }
            }

            private void validate(MavenTaskContext mavenTaskContext) throws MojoExecutionException {
                RuleSet ruleSet = readRules(mavenTaskContext);
                Analyze analyze = mavenTaskContext.getConfiguration()
                    .analyze();
                RuleSelection ruleSelection = RuleSelection.select(ruleSet, analyze.groups(), analyze.constraints(), analyze.excludeConstraints(),
                    analyze.concepts());
                RuleHelper ruleHelper = new RuleHelper();
                try {
                    ruleHelper.getAllRules(ruleSet, ruleSelection, analyze.rule());
                } catch (RuleException e) {
                    throw new MojoExecutionException("Invalid rule configuration.", e);
                }
            }

            @Override
            public void leaveProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
                withStore(store -> analyze(store, mavenTaskContext), mavenTaskContext);
            }

            private void analyze(Store store, MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
                MavenProject rootModule = mavenTaskContext.getRootModule();
                MavenConfiguration configuration = mavenTaskContext.getConfiguration();
                RuleSet ruleSet = readRules(mavenTaskContext);
                Analyze analyze = configuration.analyze();
                RuleSelection ruleSelection = RuleSelection.select(ruleSet, analyze.groups(), analyze.constraints(), analyze.excludeConstraints(),
                    analyze.concepts());
                File outputDirectory = mavenTaskContext.getOutputDirectory();

                getLog().info("Executing analysis for '" + rootModule.getName() + "'.");
                Report report = analyze.report();

                PluginRepository pluginRepository = mavenTaskContext.getPluginRepository();
                ReportContext reportContext = new ReportContextImpl(report.build(), pluginRepository.getClassLoader(), store, outputDirectory);
                AnalyzerPluginRepository analyzerPluginRepository = pluginRepository.getAnalyzerPluginRepository();
                Map<String, ReportPlugin> reportPlugins = analyzerPluginRepository.getReportPlugins(report, reportContext);
                InMemoryReportPlugin inMemoryReportPlugin = new InMemoryReportPlugin(new CompositeReportPlugin(reportPlugins));

                try {
                    Baseline baselineConfiguration = analyze.baseline();
                    BaselineRepository baselineRepository = new BaselineRepository(baselineConfiguration, mavenTaskContext.getRuleDirectory());
                    BaselineManager baselineManager = new BaselineManager(baselineConfiguration, baselineRepository);
                    Analyzer analyzer = new AnalyzerImpl(configuration.analyze(), pluginRepository.getClassLoader(), store,
                        analyzerPluginRepository.getRuleInterpreterPlugins(emptyMap()), baselineManager, inMemoryReportPlugin);
                    analyzer.execute(ruleSet, ruleSelection);
                } catch (RuleException e) {
                    throw new MojoExecutionException("Analysis failed.", e);
                }
                if (report.createArchive()) {
                    attachReportArchive(mavenTaskContext, reportContext);
                }
                ReportHelper reportHelper = new ReportHelper(report, LOGGER);
                store.beginTransaction();
                try {
                    reportHelper.verify(inMemoryReportPlugin, message -> {
                        throw new MojoFailureException(message);
                    });
                } finally {
                    store.commitTransaction();
                }
            }

            private void attachReportArchive(MavenTaskContext mavenTaskContext, ReportContext reportContext) throws MojoExecutionException {
                MavenProject currentModule = mavenTaskContext.getCurrentModule();
                MavenProject rootModule = mavenTaskContext.getRootModule();
                File reportArchive;
                try {
                    reportArchive = reportContext.createReportArchive();
                } catch (ReportException e) {
                    throw new MojoExecutionException("Cannot attach report artifact.", e);
                }
                LOGGER.info("Created report archive {}.", reportArchive);
                mavenProjectHelper.attachArtifact(rootModule, "zip", JQASSISTANT_REPORT_CLASSIFIER, reportArchive);
                if (!currentModule.equals(rootModule)) {
                    LOGGER.info(
                        "Report archive has been attached to module '{}:{}:{}'. Use 'installAtEnd' (maven-install-plugin) or 'deployAtEnd' (maven-deploy-plugin) to ensure deployment to local or remote repositories.",
                        rootModule.getGroupId(), rootModule.getArtifactId(), rootModule.getVersion());
                }
            }
        };
    }
}
