package com.buschmais.jqassistant.core.report;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
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
        assertThat(getRuleIds(html, "constraint:(.*Severity)")).containsExactly("constraint:FailureCriticalMajorSeverity",
            "constraint:WarningWithMajorSeverity", "constraint:SuccessWithMinorSeverity");
        assertThat(getRuleIds(html, "concept:(.*Severity)")).containsExactly("concept:FailureCriticalMajorSeverity", "concept:WarningWithMajorSeverity",
            "concept:SuccessWithMinorSeverity");
    }

    private static List<String> getRuleIds(String html, String rulePattern) {
        Matcher matcher = Pattern.compile(rulePattern)
            .matcher(html);
        List<String> ruleIds = new ArrayList<>();
        while (matcher.find()) {
            ruleIds.add(matcher.group(0));
        }
        return ruleIds;
    }

}
