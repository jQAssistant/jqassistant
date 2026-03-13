package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.report.api.ReportTransformer;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;
import com.buschmais.jqassistant.core.report.impl.HtmlReportTransformer;
import com.buschmais.jqassistant.core.report.impl.XmlReportPlugin;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 * Generates a HTML reports based on analysis results.
 */
@Mojo(name = "report", defaultPhase = LifecyclePhase.SITE)
public class ReportMojo extends AbstractMavenReport {

    /**
     * The file to read the XML report from.
     */
    @Parameter(property = "jqassistant.report.xml")
    protected File xmlReportFile;

    @Override
    public boolean canGenerateReport() {
        return getXmlReportFile().exists();
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        File reportFile = getXmlReportFile();
        StringWriter writer = new StringWriter();
        // Transform
        Source xmlSource = new StreamSource(reportFile);
        Result htmlTarget = new StreamResult(writer);
        getLog().info("Transforming " + reportFile.getAbsolutePath() + ".");
        ReportTransformer transformer = new HtmlReportTransformer();
        try {
            transformer.toEmbedded(xmlSource, htmlTarget);
        } catch (ReportTransformerException e) {
            throw new MavenReportException("Cannot transform report '" + reportFile + "'.", e);
        }
        getSink().rawText(writer.toString());
    }

    private File getXmlReportFile() {
        File selectedXmlReportFile;
        if (xmlReportFile != null) {
            selectedXmlReportFile = xmlReportFile;
        } else {
            selectedXmlReportFile = new File(project.getBuild()
                .getDirectory() + "/" + MavenTaskContext.OUTPUT_DIRECTORY + "/" + XmlReportPlugin.DEFAULT_XML_REPORT_FILE);
        }
        return selectedXmlReportFile;
    }

    @Override
    public String getDescription(Locale arg0) {
        return "The jQAssistant report.";
    }

    @Override
    public String getName(Locale arg0) {
        return "jQAssistant";
    }

    @Override
    public String getOutputName() {
        return "jqassistant";
    }
}
