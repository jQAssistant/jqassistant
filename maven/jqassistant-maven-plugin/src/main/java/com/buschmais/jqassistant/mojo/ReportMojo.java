package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.report.api.ReportTransformer;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;
import com.buschmais.jqassistant.core.report.impl.HtmlReportTransformer;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringWriter;
import java.util.Locale;

/**
 * @goal report
 * @phase site
 */
public class ReportMojo extends AbstractMavenReport {

    /**
     * Directory where reports will go.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    private String outputDirectory;

    /**
     * The file to write the XML report to.
     *
     * @parameter expression="${jqassistant.report.xml}"
     */
    protected File xmlReportFile;

    /**
     * @component
     * @required
     * @readonly
     */
    private Renderer siteRenderer;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;


    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        File selectedXmlReportFile;
        try {
            MavenProject baseProject = BaseProjectResolver.getBaseProject(project);
            selectedXmlReportFile = BaseProjectResolver.getReportFile(baseProject, xmlReportFile, AbstractAnalysisMojo.REPORT_XML);
        } catch (MojoExecutionException e) {
            throw new MavenReportException("Cannot resolve XML report.", e);
        }
        if (!selectedXmlReportFile.exists() || selectedXmlReportFile.isDirectory()) {
            throw new MavenReportException(selectedXmlReportFile.getAbsoluteFile() + " does not exist or is not a file.");
        }
        StringWriter writer = new StringWriter();
        // Transform
        Source xmlSource = new StreamSource(selectedXmlReportFile);
        Result htmlTarget = new StreamResult(writer);
        getLog().info("Transforming " + selectedXmlReportFile.getAbsolutePath() + ".");
        ReportTransformer transformer = new HtmlReportTransformer();
        try {
            transformer.transform(xmlSource, htmlTarget);
        } catch (ReportTransformerException e) {
            throw new MavenReportException("Cannot transform report.", e);
        }
        getSink().rawText(writer.toString());
    }

    public String getDescription(Locale arg0) {
        return "The jQAssistant report.";
    }

    public String getName(Locale arg0) {
        return "jQAssistant";
    }

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

    @Override
    protected Renderer getSiteRenderer() {
        return siteRenderer;
    }
}
