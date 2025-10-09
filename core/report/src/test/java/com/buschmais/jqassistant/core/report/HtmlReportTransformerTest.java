package com.buschmais.jqassistant.core.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.buschmais.jqassistant.core.report.api.ReportTransformerException;
import com.buschmais.jqassistant.core.report.impl.HtmlReportTransformer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the functionality of the HTML report transformer.
 */
class HtmlReportTransformerTest {

    @Test
    void reportWithSeverities() throws ReportTransformerException, IOException {
        HtmlReportTransformer transformer = new HtmlReportTransformer();
        Source xmlSource = new StreamSource(HtmlReportTransformerTest.class.getResourceAsStream("/jqassistant-report-with-severities.xml"));
        StringWriter htmlWriter = new StringWriter();
        javax.xml.transform.Result htmlTarget = new StreamResult(htmlWriter);

        transformer.toEmbedded(xmlSource, htmlTarget);

        String html = htmlWriter.toString();
        saveHtml(html);
        assertThat(getRuleIds(html, "constraint:([a-zA-Z]*Severity)")).containsExactlyInAnyOrder("constraint:FailureCriticalMajorSeverity",
            "constraint:WarningWithMajorSeverity", "constraint:SuccessWithMinorSeverity");
        assertThat(getRuleIds(html, "concept:([a-zA-Z]*Severity)")).containsExactlyInAnyOrder("concept:FailureCriticalMajorSeverity",
            "concept:WarningWithMajorSeverity", "concept:SuccessWithMinorSeverity");
    }

    private static Set<String> getRuleIds(String html, String rulePattern) {
        Matcher matcher = Pattern.compile(rulePattern)
            .matcher(html);
        Set<String> ruleIds = new TreeSet<>();
        while (matcher.find()) {
            ruleIds.add(matcher.group(0));
        }
        return ruleIds;
    }

    /** for testing purposes, delete later **/

    public void saveHtml (String html) throws IOException {
        String filePath = "target/reportHtml";
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(html);
            writer.close();
    }

}
