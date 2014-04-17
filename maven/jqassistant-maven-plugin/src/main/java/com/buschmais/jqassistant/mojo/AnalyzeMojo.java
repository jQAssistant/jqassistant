package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.report.JUnitReportWriter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Runs analysis according to the defined rules.
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY)
public class AnalyzeMojo extends AbstractAnalysisAggregatorMojo {

    /**
     * Defines the supported report types.
     */
    public enum ReportType {
        JQA,
        JUNIT;
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
        List<ExecutionListener> reportWriters = new LinkedList<>();
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
                    } catch (ExecutionListenerException e) {
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
        Analyzer analyzer = new AnalyzerImpl(store, reportWriter);
        try {
            analyzer.execute(ruleSet);
        } catch (AnalyzerException e) {
            throw new MojoExecutionException("Analysis failed.", e);
        }
        store.beginTransaction();
        try {
            verifyConceptResults(inMemoryReportWriter);
            verifyConstraintViolations(inMemoryReportWriter);
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
        } catch (ExecutionListenerException e) {
            throw new MojoExecutionException("Cannot create XML report file writer.", e);
        }
        return junitReportWriter;
    }

    /**
     * Verifies the concept results returned by the {@link InMemoryReportWriter}
     * .
     * <p>
     * A warning is logged for each concept which did not return a result (i.e.
     * has not been applied).
     * </p>
     *
     * @param inMemoryReportWriter The {@link InMemoryReportWriter}.
     */
    private void verifyConceptResults(InMemoryReportWriter inMemoryReportWriter) {
        List<Result<Concept>> conceptResults = inMemoryReportWriter.getConceptResults();
        for (Result<Concept> conceptResult : conceptResults) {
            if (conceptResult.getRows().isEmpty()) {
                getLog().warn("Concept '" + conceptResult.getExecutable().getId() + "' returned an empty result.");
            }
        }
    }

    /**
     * Verifies the constraint violations returned by the
     * {@link InMemoryReportWriter}.
     *
     * @param inMemoryReportWriter The {@link InMemoryReportWriter}.
     * @throws MojoFailureException If constraint violations are detected.
     */
    private void verifyConstraintViolations(InMemoryReportWriter inMemoryReportWriter) throws MojoFailureException {
        List<Result<Constraint>> constraintViolations = inMemoryReportWriter.getConstraintViolations();
        int violations = 0;
        for (Result<Constraint> constraintViolation : constraintViolations) {
            if (!constraintViolation.isEmpty()) {
                AbstractExecutable constraint = constraintViolation.getExecutable();
                getLog().error(constraint.getId() + ": " + constraint.getDescription());
                for (Map<String, Object> columns : constraintViolation.getRows()) {
                    StringBuilder message = new StringBuilder();
                    for (Map.Entry<String, Object> entry : columns.entrySet()) {
                        if (message.length() > 0) {
                            message.append(", ");
                        }
                        message.append(entry.getKey());
                        message.append('=');
                        Object value = entry.getValue();
                        message.append(value instanceof FullQualifiedNameDescriptor ? ((FullQualifiedNameDescriptor) value).getFullQualifiedName() : value.toString());
                    }
                    getLog().error("  " + message.toString());
                }
                violations++;
            }
        }
        if (failOnConstraintViolations && violations > 0) {
            throw new MojoFailureException(violations + " constraints have been violated!");
        }
    }

    /**
     * Returns the {@link File} to write the XML report to.
     *
     * @return The {@link File} to write the XML report to.
     * @throws MojoExecutionException If the file cannot be determined.
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
