package com.buschmais.jqassistant.scm.cli.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.CommandLine;

import com.buschmais.jqassistant.core.report.api.ReportTransformer;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;
import com.buschmais.jqassistant.core.report.impl.HtmlReportTransformer;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.cli.CliExecutionException;
import com.buschmais.jqassistant.scm.cli.Log;

public class ReportTask extends AbstractTask {

    public static final String REPORT_FILE_HTML = "jqassistant-report.html";

    private String reportDirectory;

    @Override
    protected void executeTask(final Store store) throws CliExecutionException {
        File xmlReportFile = new File(reportDirectory, REPORT_FILE_XML);
        if (!xmlReportFile.exists()) {
            Log.getLog().error(xmlReportFile.getName() + " does not exist.");
        } else {
            Log.getLog().info("Transforming " + xmlReportFile.getAbsolutePath() + ".");
            File htmlReportFile = new File(reportDirectory, REPORT_FILE_HTML);
            Source xmlSource = new StreamSource(xmlReportFile);
            FileWriter writer;
            try {
                writer = new FileWriter(htmlReportFile);
            } catch (IOException e) {
                throw new CliExecutionException("Cannot create HTML report file.", e);
            }
            Result htmlTarget = new StreamResult(writer);
            ReportTransformer transformer = new HtmlReportTransformer();
            try {
                transformer.toStandalone(xmlSource, htmlTarget);
            } catch (ReportTransformerException e) {
                throw new CliExecutionException("Cannot transform report.", e);
            }
        }
    }

    @Override
    public void withOptions(CommandLine options) {
        reportDirectory = getOptionValue(options, CMDLINE_OPTION_REPORTDIR, DEFAULT_REPORT_DIRECTORY);
    }
}
