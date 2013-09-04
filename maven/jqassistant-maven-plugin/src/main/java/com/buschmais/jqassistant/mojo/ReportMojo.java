package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.report.api.ReportTransformer;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;
import com.buschmais.jqassistant.core.report.impl.HtmlReportTransformer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

/**
 * @goal report
 * @phase site
 */
public class ReportMojo extends AbstractAnalysisAggregatorMojo {

    public static final String REPORT_HTML = "/jqassistant/jqassistant-report.html";

    /**
     * The file to write the XML report to.
     *
     * @parameter expression="${jqassistant.report.html}"
     */
    protected File htmlReportFile;

    @Override
    public void aggregate() throws MojoExecutionException, MojoFailureException {
        // Determine XML report file
        File selectedXmlReportFile = BaseProjectResolver.getReportFile(project, xmlReportFile, REPORT_XML);
        if (!selectedXmlReportFile.exists() || selectedXmlReportFile.isDirectory()) {
            throw new MojoExecutionException(selectedXmlReportFile.getAbsoluteFile() + " does not exist or is not a file.");
        }
        // Determine HTML report file
        File selectedHtmlReportFile = BaseProjectResolver.getReportFile(project, htmlReportFile, REPORT_HTML);
        selectedHtmlReportFile.getParentFile().mkdirs();
        // Transform
        Source xmlSource = new StreamSource(selectedXmlReportFile);
        Result htmlTarget = new StreamResult(selectedHtmlReportFile);
        getLog().info("Transforming " + selectedXmlReportFile.getAbsolutePath() + " to " + selectedHtmlReportFile.getAbsolutePath() + ".");
        ReportTransformer transformer = new HtmlReportTransformer();
        try {
            transformer.transform(xmlSource, htmlTarget);
        } catch (ReportTransformerException e) {
            throw new MojoExecutionException("Cannot transform report.", e);
        }
    }
}
