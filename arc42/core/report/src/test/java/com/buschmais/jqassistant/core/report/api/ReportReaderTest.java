package com.buschmais.jqassistant.core.report.api;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.jqassistant.schema.report.v2.ConceptType;
import org.jqassistant.schema.report.v2.GroupType;
import org.jqassistant.schema.report.v2.JqassistantReport;
import org.jqassistant.schema.report.v2.ReferencableRuleType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportReaderTest {

    private final ReportReader reportReader = new ReportReader();

    @Test
    void read() {
        URL reportUrl = ReportReaderTest.class.getResource("/jqassistant-report-with-reports.xml");
        File reportFile = new File(reportUrl.getFile());

        JqassistantReport jqassistantReport = reportReader.read(reportFile);

        assertThat(jqassistantReport).isNotNull();
        List<ReferencableRuleType> groupOrConceptOrConstraint = jqassistantReport.getGroupOrConceptOrConstraint();
        assertThat(groupOrConceptOrConstraint).hasSize(1);
        ReferencableRuleType referencableRuleType = groupOrConceptOrConstraint.get(0);
        assertThat(referencableRuleType).isInstanceOf(GroupType.class);
        GroupType groupType = (GroupType) referencableRuleType;
        assertThat(groupType.getId()).isEqualTo("default");
        List<ReferencableRuleType> groupOrConceptOrConstraint1 = groupType.getGroupOrConceptOrConstraint();
        assertThat(groupOrConceptOrConstraint1).hasSize(1);
        ReferencableRuleType referencableRuleType1 = groupOrConceptOrConstraint1.get(0);
        assertThat(referencableRuleType1).isInstanceOf(ConceptType.class);
        ConceptType conceptType = (ConceptType) referencableRuleType1;
        assertThat(conceptType.getId()).isEqualTo("concept-with-reports");
    }

}
