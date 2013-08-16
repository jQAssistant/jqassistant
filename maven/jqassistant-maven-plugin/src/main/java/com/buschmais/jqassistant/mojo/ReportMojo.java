package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.report.api.ReportTransformer;
import com.buschmais.jqassistant.core.report.api.ReportTransformerException;
import com.buschmais.jqassistant.core.report.impl.HtmlReportTransformer;
import org.apache.maven.plugin.AbstractMojo;
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
 * @requiresProject false
 */
public class ReportMojo extends AbstractMojo {

    /**
     * The file to write the XML report to.
     *
     * @parameter expression="${jqassistant.report.xml}" default-value="${project.build.directory}/jqassistant/jqassistant-report.xml"
     */
    protected File xmlReportFile;

    /**
     * The file to write the XML report to.
     *
     * @parameter expression="${jqassistant.report.html}" default-value="${project.build.directory}/jqassistant/jqassistant-report.html"
     */
    protected File htmlReportFile;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!xmlReportFile.exists() || xmlReportFile.isDirectory()) {
            throw new MojoExecutionException(xmlReportFile.getAbsoluteFile() + " does not exist or is not a file.");
        }
        htmlReportFile.getParentFile().mkdirs();
        Source xmlSource = new StreamSource(xmlReportFile);
        Result htmlTarget = new StreamResult(htmlReportFile);
        getLog().info("Transforming " + xmlReportFile.getAbsolutePath() + " to " + htmlReportFile.getAbsolutePath() + ".");
        ReportTransformer transformer = new HtmlReportTransformer();
        try {
            transformer.transform(xmlSource, htmlTarget);
        } catch (ReportTransformerException e) {
            throw new MojoExecutionException("Cannot transform report.", e);
        }
    }
}
