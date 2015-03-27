package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.io.StringWriter;
import java.util.Locale;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import com.buschmais.jqassistant.core.report.api.ReportTransformer;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;
import com.buschmais.jqassistant.core.report.impl.HtmlReportTransformer;

/**
 * Generates a HTML reports based on analysis results.
 */
@Mojo(name = "report", defaultPhase = LifecyclePhase.SITE)
public class ReportMojo extends AbstractMavenReport {

    /**
     * Directory where reports will go.
     */
    @Parameter(property = "project.reporting.outputDirectory")
    protected String outputDirectory;

    /**
     * The file to read the XML report from.
     */
    @Parameter(property = "jqassistant.report.xml")
    protected File xmlReportFile;

    /**
     * The Maven project.
     */
    @Parameter(property = "project")
    protected MavenProject project;

    @Component
    protected Renderer siteRenderer;

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        File selectedXmlReportFile;
        if (xmlReportFile != null) {
            selectedXmlReportFile = xmlReportFile;
        } else {
            selectedXmlReportFile = new File(project.getBuild().getDirectory() + "/" + ProjectResolver.OUTPUT_DIRECTORY + "/" + AbstractMojo.REPORT_XML);
        }
        if (selectedXmlReportFile.exists()) {
            StringWriter writer = new StringWriter();
            // Transform
            Source xmlSource = new StreamSource(selectedXmlReportFile);
            Result htmlTarget = new StreamResult(writer);
            getLog().info("Transforming " + selectedXmlReportFile.getAbsolutePath() + ".");
            ReportTransformer transformer = new HtmlReportTransformer();
            try {
                transformer.toEmbedded(xmlSource, htmlTarget);
            } catch (ReportTransformerException e) {
                throw new MavenReportException("Cannot transform report.", e);
            }
            getSink().rawText(writer.toString());
        }
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

    @Override
    protected String getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    @Override
    protected Renderer getSiteRenderer() {
        return siteRenderer;
    }
}
