package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;
import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.common.report.ReportHelper;
import com.buschmais.jqassistant.scm.maven.report.JUnitReportWriter;

/**
 * Runs analysis according to the defined rules.
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY)
public class AnalyzeMojo extends AbstractProjectMojo {

    /**
     * Defines the supported report types.
     */
    public enum ReportType {
        JQA, JUNIT
    }

    /**
     * Indicates if the plugin shall fail if a constraint violation is detected.
     */
    @Parameter(property = "jqassistant.failOnConstraintViolations", defaultValue = "false")
    protected boolean failOnConstraintViolations;

    /**
     * Severity level for constraint violation failure check. Default value is
     * {@code Severity.INFO}
     */
    @Parameter(property = "jqassistant.severity", defaultValue = "info")
    protected String severity;

    @Parameter(property = "jqassistant.junitReportDirectory")
    private java.io.File junitReportDirectory;

    @Parameter(property = "jqassistant.reportTypes")
    private List<ReportType> reportTypes;

    @Parameter(property = "jqassistant.reportProperties")
    private Map<String, Object> reportProperties;

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    public void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing analysis for '" + rootModule.getName() + "'.");

        // perform validations
        validateParameters();

        final RuleSet ruleSet = resolveEffectiveRules(rootModule);
        List<AnalysisListener> reportWriters = new LinkedList<>();
        InMemoryReportWriter inMemoryReportWriter = new InMemoryReportWriter();
        reportWriters.add(inMemoryReportWriter);
        if (reportTypes == null || reportTypes.isEmpty()) {
            reportTypes = Arrays.asList(ReportType.JQA);
        }
        for (ReportType reportType : reportTypes) {
            switch (reportType) {
            case JQA:
                FileWriter xmlReportFileWriter;
                try {
                    xmlReportFileWriter = new FileWriter(getXmlReportFile(rootModule));
                } catch (IOException e) {
                    throw new MojoExecutionException("Cannot create XML report file.", e);
                }
                XmlReportWriter xmlReportWriter;
                try {
                    xmlReportWriter = new XmlReportWriter(xmlReportFileWriter);
                } catch (AnalysisListenerException e) {
                    throw new MojoExecutionException("Cannot create XML report file writer.", e);
                }
                reportWriters.add(xmlReportWriter);
                break;
            case JUNIT:
                reportWriters.add(getJunitReportWriter(rootModule));
                break;
            }
        }
        Map<String, Object> properties = reportProperties != null ? reportProperties : Collections.<String, Object> emptyMap();
        List<ReportPlugin> reportPlugins = pluginRepositoryProvider.getReportPluginRepository(properties).getReportPlugins();
        reportWriters.addAll(reportPlugins);
        CompositeReportWriter reportWriter = new CompositeReportWriter(reportWriters);
        MavenConsole console = new MavenConsole(getLog());
        Analyzer analyzer = new AnalyzerImpl(store, reportWriter, console);
        try {
            analyzer.execute(ruleSet);
        } catch (AnalysisException e) {
            throw new MojoExecutionException("Analysis failed.", e);
        }
        ReportHelper reportHelper = new ReportHelper(console);
        store.beginTransaction();
        try {
            int conceptViolations = reportHelper.verifyConceptResults(inMemoryReportWriter);
            if (failOnConstraintViolations && conceptViolations > 0) {
                throw new MojoFailureException(conceptViolations + " concept(s) returned empty results!");
            }
            int constraintViolations = reportHelper.verifyConstraintViolations(Severity.fromValue(severity), inMemoryReportWriter);
            if (failOnConstraintViolations && constraintViolations > 0) {
                throw new MojoFailureException(constraintViolations + " constraint(s) have been violated!");
            }
        } finally {
            store.commitTransaction();
        }
    }

    private JUnitReportWriter getJunitReportWriter(MavenProject baseProject) throws MojoExecutionException {
        JUnitReportWriter junitReportWriter;
        if (junitReportDirectory == null) {
            junitReportDirectory = new File(baseProject.getBuild().getDirectory() + "/surefire-reports");
        }
        junitReportDirectory.mkdirs();
        try {
            junitReportWriter = new JUnitReportWriter(junitReportDirectory);
        } catch (AnalysisListenerException e) {
            throw new MojoExecutionException("Cannot create XML report file writer.", e);
        }
        return junitReportWriter;
    }

    /**
     * Returns the {@link File} to write the XML report to.
     * 
     * @return The {@link File} to write the XML report to.
     * @throws MojoExecutionException
     *             If the file cannot be determined.
     */
    private File getXmlReportFile(MavenProject baseProject) throws MojoExecutionException {
        File selectedXmlReportFile = ProjectResolver.getOutputFile(baseProject, xmlReportFile, REPORT_XML);
        selectedXmlReportFile.getParentFile().mkdirs();
        return selectedXmlReportFile;
    }

    /**
     * Check parameter values.
     * 
     * @throws MojoFailureException
     *             if any parameter is wrong or missing.
     */
    private void validateParameters() throws MojoFailureException {
        if (Severity.fromValue(severity) == null) {
            throw new MojoFailureException("Invalid severity '" + severity + "'; use one of " + Arrays.toString(Severity.names()));
        }
    }

}
