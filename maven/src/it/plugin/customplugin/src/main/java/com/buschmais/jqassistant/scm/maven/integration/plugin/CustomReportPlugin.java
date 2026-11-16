package com.buschmais.jqassistant.scm.maven.integration.plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

public class CustomReportPlugin implements ReportPlugin {
    private static final String PROPERTY_FILENAME = "customReport.fileName";

    private String fileName;

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) throws ReportException {
        this.fileName = (String) properties.get(PROPERTY_FILENAME);
        if (this.fileName == null) {
            throw new ReportException("Property " + PROPERTY_FILENAME + " is not specified.");
        }
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        Properties properties = result.getRule().getReport().getProperties();
        String suffix = properties.getProperty("suffix");
        try(
            final FileOutputStream fos = new FileOutputStream(suffix != null ? fileName + "." + suffix : fileName, true);
            final PrintWriter writer = new PrintWriter(fos, false, StandardCharsets.UTF_8)
        ) {
            writer.print(result.getRule().getId());
            writer.print(":");
            writer.print(result.getStatus().name());
            writer.println();
        } catch (IOException e) {
            throw new ReportException("Cannot write custom report.", e);
        }
    }
}
