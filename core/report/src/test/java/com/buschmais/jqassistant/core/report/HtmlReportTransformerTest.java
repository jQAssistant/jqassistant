package com.buschmais.jqassistant.core.report;

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
    void reportWithSeverities() throws ReportTransformerException {
        HtmlReportTransformer transformer = new HtmlReportTransformer();
        Source xmlSource = new StreamSource(HtmlReportTransformerTest.class.getResourceAsStream("/jqassistant-report-with-severities.xml"));
        StringWriter htmlWriter = new StringWriter();
        javax.xml.transform.Result htmlTarget = new StreamResult(htmlWriter);

        transformer.toEmbedded(xmlSource, htmlTarget);

        String html = htmlWriter.toString();
        assertThat(getRuleIds(html, "constraint:([a-zA-Z]*Severity)")).containsExactlyInAnyOrder("constraint:FailureCriticalMajorSeverity",
            "constraint:WarningWithMajorSeverity", "constraint:SuccessWithMinorSeverity");
        assertThat(getRuleIds(html, "concept:([a-zA-Z]*Severity)")).containsExactlyInAnyOrder("concept:FailureCriticalMajorSeverity",
            "concept:WarningWithMajorSeverity", "concept:SuccessWithMinorSeverity");
    }

    @Test
    void reportWithOverrides() throws ReportTransformerException {
        HtmlReportTransformer transformer = new HtmlReportTransformer();
        Source xmlSource = new StreamSource(HtmlReportTransformerTest.class.getResourceAsStream("/jqassistant-report-with-overrides.xml"));
        StringWriter htmlWriter = new StringWriter();
        javax.xml.transform.Result htmlTarget = new StreamResult(htmlWriter);
        transformer.toEmbedded(xmlSource, htmlTarget);

        String html = htmlWriter.toString();
        assertThat(getRuleIds(html, "([a-zA-Z]*):Overridden([A-Za-z0-9]*)")).containsExactlyInAnyOrder("concept:OverriddenConcept1",
                "concept:OverriddenConcept2", "constraint:OverriddenConstraint", "group:OverriddenGroup1", "group:OverriddenGroup2",
                "resultOfconcept:OverriddenConcept1", "resultOfconcept:OverriddenConcept2", "resultOfconstraint:OverriddenConstraint");
    }

    @Test
    void reportWithHiddenRows() throws ReportTransformerException {
        HtmlReportTransformer transformer = new HtmlReportTransformer();
        Source xmlSource = new StreamSource(HtmlReportTransformerTest.class.getResourceAsStream("/jqassistant-report-with-hidden-elements.xml"));
        StringWriter htmlWriter = new StringWriter();
        javax.xml.transform.Result htmlTarget = new StreamResult(htmlWriter);
        transformer.toEmbedded(xmlSource, htmlTarget);
        String html = htmlWriter.toString();

        assertThat(getRuleIds(html, "concept:([A-Za-z0-9]*)HiddenRows")).containsExactlyInAnyOrder("concept:ConceptWithHiddenRows");
        assertThat(getRuleIds(html, "constraint:([A-Za-z0-9]*)HiddenRows")).containsExactlyInAnyOrder("constraint:ConstraintWithHiddenRows");
        assertThat(html.contains("suppressed by baseline")).isTrue();
        assertThat(html.contains("suppressed by suppression")).isTrue();
        assertThat(html.contains("Description for the reason of this suppression.")).isTrue();
        assertThat(html.contains("2065-11-11")).isTrue();
        assertThat(html.contains("Second description for the reason of this suppression.")).isTrue();
        assertThat(html.contains("2075-02-04")).isTrue();
        assertThat(html.contains("suppressed by baseline and suppression")).isTrue();

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
}
