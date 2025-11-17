package com.buschmais.jqassistant.core.test;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.model.AnalyzeTaskDescriptor;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.analysis.api.model.ConstraintDescriptor;
import com.buschmais.jqassistant.core.analysis.api.model.GroupDescriptor;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;
import static org.assertj.core.api.Assertions.assertThat;

public class AnalyzeAuditIT extends AbstractPluginIT {

    @Test
    void auditGraph() throws RuleException {
        executeGroup("core-test-audit:Group");
        store.beginTransaction();
        TestResult query = query(
            "MATCH (task:jQAssistant:Task)-[:INCLUDES_GROUP]->(group)-[:INCLUDES_CONSTRAINT]->(constraint:jQAssistant:Constraint)-[:REQUIRES_CONCEPT]->(concept:jQAssistant:Concept) RETURN task, group, constraint, concept");
        List<Map<String, Object>> rows = query.getRows();
        assertThat(rows).hasSize(1);
        Map<String, Object> row = rows.get(0);
        AnalyzeTaskDescriptor analyzeTask = (AnalyzeTaskDescriptor) row.get("task");
        assertThat(analyzeTask).isNotNull();
        assertThat(analyzeTask.getTimestamp()).isNotNull();

        GroupDescriptor group = (GroupDescriptor) row.get("group");
        assertThat(group).isNotNull();
        assertThat(group.getId()).isEqualTo("core-test-audit:Group");
        assertThat(group.getSeverity()).isNull();
        assertThat(group.getEffectiveSeverity()).isNull();
        assertThat(analyzeTask.getIncludesGroups()).contains(group);
        assertThat(analyzeTask.getIncludesConcepts()).isEmpty();
        assertThat(analyzeTask.getIncludesConstraints()).isEmpty();

        ConceptDescriptor concept = (ConceptDescriptor) row.get("concept");
        assertThat(concept).isNotNull();
        assertThat(concept.getId()).isEqualTo("core-test-audit:Concept");
        assertThat(concept.getStatus()).isEqualTo(SUCCESS);
        assertThat(group.getSeverity()).isNull();
        assertThat(group.getEffectiveSeverity()).isNull();
        assertThat(concept.getRequiresConcepts()).isEmpty();
        assertThat(concept.getProvidesConcepts()).isEmpty();

        List<ConceptDescriptor> providingConcepts = concept.getProvidingConcepts();
        assertThat(providingConcepts).hasSize(1);
        ConceptDescriptor providingConcept = providingConcepts.get(0);
        assertThat(providingConcept.getId()).isEqualTo("core-test-audit:ProvidingConcept");
        assertThat(providingConcept.getStatus()).isEqualTo(SUCCESS);
        assertThat(providingConcept.getSeverity()).isEqualTo(MINOR);
        assertThat(providingConcept.getEffectiveSeverity()).isEqualTo(MINOR);

        ConstraintDescriptor constraint = (ConstraintDescriptor) row.get("constraint");
        assertThat(constraint).isNotNull();
        assertThat(constraint.getId()).isEqualTo("core-test-audit:Constraint");
        assertThat(constraint.getStatus()).isEqualTo(FAILURE);
        assertThat(group.getSeverity()).isNull();
        assertThat(group.getEffectiveSeverity()).isNull();
        assertThat(group.getIncludesConstraints()).contains(constraint);
        assertThat(constraint.getRequiresConcepts()).contains(concept);

        store.commitTransaction();
    }
}
