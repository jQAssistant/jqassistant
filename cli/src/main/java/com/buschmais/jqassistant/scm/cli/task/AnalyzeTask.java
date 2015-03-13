package com.buschmais.jqassistant.scm.cli.task;

import static com.buschmais.jqassistant.scm.cli.Log.getLog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.IOUtils;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.impl.CompositeReportWriter;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.report.impl.XmlReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.CliRuleViolationException;
import com.buschmais.jqassistant.scm.cli.Log;
import com.buschmais.jqassistant.scm.common.report.ReportHelper;

/**
 * @author jn4, Kontext E GmbH, 24.01.14
 */
public class AnalyzeTask extends AbstractAnalyzeTask {

    private static final String CMDLINE_OPTION_SEVERITY = "severity";

    private static final Console LOG = Log.getLog();

    private String reportDirectory;
    private Severity severity;

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        LOG.info("Executing analysis.");
        InMemoryReportWriter inMemoryReportWriter = new InMemoryReportWriter();
        FileWriter xmlReportFileWriter;
        try {
            xmlReportFileWriter = new FileWriter(getXmlReportFile());
        } catch (IOException e) {
            throw new RuntimeException("Cannot create XML report file.", e);
        }
        XmlReportWriter xmlReportWriter;
        try {
            xmlReportWriter = new XmlReportWriter(xmlReportFileWriter);
        } catch (AnalysisListenerException e) {
            throw new RuntimeException("Cannot create XML report file writer.", e);
        }
        List<AnalysisListener> reportWriters = new LinkedList<>();
        reportWriters.add(inMemoryReportWriter);
        reportWriters.add(xmlReportWriter);
        try {
            CompositeReportWriter reportWriter = new CompositeReportWriter(reportWriters);
            Analyzer analyzer = new AnalyzerImpl(store, reportWriter, getLog());
            try {
                RuleSet availableRules = getAvailableRules();
                analyzer.execute(availableRules, getRuleSelection(availableRules));
            } catch (AnalysisException e) {
                throw new CliExecutionException("Analysis failed.", e);
            }
        } finally {
            IOUtils.closeQuietly(xmlReportFileWriter);
        }
        store.beginTransaction();
        Log.getLog().info("Verifying results, severity=" + severity);
        try {
            final ReportHelper reportHelper = new ReportHelper(getLog());
            final int conceptViolations = reportHelper.verifyConceptResults(severity, inMemoryReportWriter);
            if (conceptViolations > 0) {
                throw new CliRuleViolationException(conceptViolations + " concept(s) returned empty results!");
            }
            final int violations = reportHelper.verifyConstraintResults(severity, inMemoryReportWriter);
            if (violations > 0) {
                throw new CliRuleViolationException(violations + " constraint(s) violated!");
            }
        } finally {
            store.commitTransaction();
        }
    }

    /**
     * Returns the {@link java.io.File} to write the XML report to.
     *
     * @return The {@link java.io.File} to write the XML report to.
     */
    private File getXmlReportFile() {
        File reportFile = new File(reportDirectory, REPORT_FILE_XML);
        reportFile.getParentFile().mkdirs();
        return reportFile;
    }

    @Override
    public void withOptions(final CommandLine options) {
        super.withOptions(options);
        reportDirectory = getOptionValue(options, CMDLINE_OPTION_REPORTDIR, DEFAULT_REPORT_DIRECTORY);
        severity = Severity.valueOf(getOptionValue(options, CMDLINE_OPTION_SEVERITY, Severity.CRITICAL.name()).toUpperCase());
    }

    @Override
    protected void addTaskOptions(final List<Option> options) {
        super.addTaskOptions(options);
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_REPORTDIR).withDescription("The directory for writing reports.").hasArgs()
                .create(CMDLINE_OPTION_REPORTDIR));
        options.add(OptionBuilder.withArgName(CMDLINE_OPTION_SEVERITY).withDescription("The severity threshold to report a failure.").hasArgs()
                .create(CMDLINE_OPTION_SEVERITY));
    }
}
