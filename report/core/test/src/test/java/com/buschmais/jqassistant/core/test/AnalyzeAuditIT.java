package com.buschmais.jqassistant.core.test;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.model.*;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.*;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MAJOR;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;
import static org.assertj.core.api.Assertions.assertThat;

public class AnalyzeAuditIT extends AbstractPluginIT {

    @Test
    void auditGraph() throws RuleException {
        executeGroup("core-test-audit:Group");
        store.beginTransaction();
        TestResult query = query("MATCH" //
            + "  (task:jQAssistant:Task)-[:INCLUDES_GROUP]->(group), " //
            + "  (group)-[:INCLUDES_CONSTRAINT]->(constraint:jQAssistant:Constraint)," //
            + "  (constraint)<-[:OVERRIDES_CONSTRAINT]-(overridingConstraint)," //
            + "  (overridingConstraint)-[:REQUIRES_CONCEPT]->(concept:jQAssistant:Concept)," //
            + "  (concept)<-[:OVERRIDES_CONCEPT]-(overridingConcept:Concept)"  //
            + "RETURN " + "  task, group, constraint, overridingConstraint, concept, overridingConcept");
        List<Map<String, Object>> rows = query.getRows();
        assertThat(rows).hasSize(1);
        Map<String, Object> row = rows.get(0);

        ConceptDescriptor concept = (ConceptDescriptor) row.get("concept");
        verify(concept, "core-test-audit:Concept", MINOR, SKIPPED);
        assertThat(concept.getRequiresConcepts()).isEmpty();
        assertThat(concept.getProvidesConcepts()).isEmpty();

        ConceptDescriptor overridingConcept = (ConceptDescriptor) row.get("overridingConcept");
        verify(overridingConcept, "core-test-audit:OverridingConcept", MINOR, SUCCESS);
        assertThat(overridingConcept.getRequiresConcepts()).isEmpty();
        assertThat(overridingConcept.getProvidesConcepts()).isEmpty();

        List<ConceptDescriptor> providingConcepts = overridingConcept.getProvidingConcepts();
        assertThat(providingConcepts).hasSize(1);
        ConceptDescriptor providingConcept = providingConcepts.get(0);
        verify(providingConcept, "core-test-audit:ProvidingConcept", MINOR, SUCCESS);

        ConstraintDescriptor constraint = (ConstraintDescriptor) row.get("constraint");
        verify(constraint, "core-test-audit:Constraint", MAJOR, SKIPPED);
        assertThat(constraint.getRequiresConcepts()).isEmpty();

        ConstraintDescriptor overridingConstraint = (ConstraintDescriptor) row.get("overridingConstraint");
        verify(overridingConstraint, "core-test-audit:OverridingConstraint", MAJOR, FAILURE);
        assertThat(overridingConstraint.getRequiresConcepts()).contains(concept);

        GroupDescriptor group = (GroupDescriptor) row.get("group");
        assertThat(group).isNotNull();
        assertThat(group.getId()).isEqualTo("core-test-audit:Group");
        assertThat(group.getSeverity()).isNull();
        assertThat(group.getEffectiveSeverity()).isNull();
        assertThat(group.getTimestamp()).isNotNull();
        assertThat(group.getIncludesGroups()).isEmpty();
        assertThat(group.getIncludesConcepts()).isEmpty();
        assertThat(group.getIncludesConstraints()).contains(constraint);

        AnalyzeTaskDescriptor analyzeTask = (AnalyzeTaskDescriptor) row.get("task");
        assertThat(analyzeTask).isNotNull();
        assertThat(analyzeTask.getTimestamp()).isNotNull();
        assertThat(analyzeTask.getIncludesGroups()).contains(group);
        assertThat(analyzeTask.getIncludesConcepts()).isEmpty();
        assertThat(analyzeTask.getIncludesConstraints()).isEmpty();
        store.commitTransaction();
    }

    private static <D extends RuleDescriptor & ExecutableRuleTemplate> void verify(D ruleDescriptor, String expectedId, Severity expectedSeverity,
        Result.Status expectedStatus) {
        assertThat(ruleDescriptor).isNotNull();
        assertThat(ruleDescriptor.getId()).isEqualTo(expectedId);
        assertThat(ruleDescriptor.getStatus()).isEqualTo(expectedStatus);
        assertThat(ruleDescriptor.getSeverity()).isEqualTo(expectedSeverity);
        assertThat(ruleDescriptor.getEffectiveSeverity()).isEqualTo(expectedSeverity);
        assertThat(ruleDescriptor.getTimestamp()).isNotNull();
    }
}
