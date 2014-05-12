package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.maven.report.JUnitReportWriter;

/**
 * Runs analysis according to the defined rules.
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY)
public class AnalyzeMojo extends AbstractAnalysisMojo {

    /**
     * Defines the supported report types.
     */
    public enum ReportType {
        JQA, JUNIT
    }

    /**
     * Indicates if the plugin shall fail if a constraint violation is detected.
     */
    @Parameter(property = "jqassistant.failOnConstraintViolations", defaultValue = "true")
    protected boolean failOnConstraintViolations;

    @Parameter(property = "jqassistant.junitReportDirectory")
    private java.io.File junitReportDirectory;

    @Parameter(property = "jqassistant.reportTypes")
    private List<ReportType> reportTypes;

    @Override
    public void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException {
        getLog().info("Executing analysis for '" + baseProject.getName() + "'.");
        final RuleSet ruleSet = resolveEffectiveRules(baseProject);
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
                    xmlReportFileWriter = new FileWriter(getXmlReportFile(baseProject));
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
                reportWriters.add(getJunitReportWriter(baseProject));
                break;
            }
        }
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
            reportHelper.verifyConceptResults(inMemoryReportWriter);
            int violations = reportHelper.verifyConstraintViolations(inMemoryReportWriter);
            if (failOnConstraintViolations && violations > 0) {
                throw new MojoFailureException(violations + " constraints have been violated!");
            }
        } catch (AnalysisListenerException e) {
            throw new MojoExecutionException("Cannot print report.", e);
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
        File selectedXmlReportFile = BaseProjectResolver.getOutputFile(baseProject, xmlReportFile, REPORT_XML);
        selectedXmlReportFile.getParentFile().mkdirs();
        return selectedXmlReportFile;
    }

    @Override
    protected boolean isResetStoreOnInitialization() {
        return false;
    }
}
