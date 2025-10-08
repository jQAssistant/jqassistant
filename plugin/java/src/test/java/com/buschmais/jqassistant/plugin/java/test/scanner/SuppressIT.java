package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.JavaSuppressDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress.Suppress;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

class SuppressIT extends AbstractJavaPluginIT {

    @Test
    void suppressAnnotationWithUntilAndReasonAttributes() {
        scanClasses(Suppress.class);
        List<Map<String, Object>> rows = query("MATCH (type:Java:jQASuppress) return type" ).getRows();
        assertThat(rows.size()).isEqualTo(3);
        store.beginTransaction();
        assertThat(((JavaSuppressDescriptor) rows.get(0).get("type")).getSuppressReason()).isEqualTo("For testing this annotation");
        assertThat(((JavaSuppressDescriptor) rows.get(0).get("type")).getSuppressUntil()).isNull();
        assertThat(((JavaSuppressDescriptor) rows.get(1).get("type")).getSuppressReason()).isNull();
        assertThat(((JavaSuppressDescriptor) rows.get(1).get("type")).getSuppressUntil()).isNull();
        assertThat(((JavaSuppressDescriptor) rows.get(2).get("type")).getSuppressReason()).isEqualTo("Reason for suppression");
        assertThat(((JavaSuppressDescriptor) rows.get(2).get("type")).getSuppressUntil()).isEqualTo("2075-08-13");
        store.commitTransaction();
    }


    @Test
    void suppressAnnotationMustNotBeScanned() throws RuleException {
        scanClasses(Suppress.class);
        Result<Constraint> constraintResult = validateConstraint("test-suppress:SuppressAnnotationMustNotBeScanned");
        assertThat(constraintResult.getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        assertThat(constraintResult.getRows().size()).isEqualTo(0);
        store.commitTransaction();
    }

    @Test
    void suppressedClass() throws RuleException {
        verifySuppress("test-suppress:Class", "test-suppress:SuppressedClass", "class");
    }

    @Test
    void suppressedField() throws RuleException {
        verifySuppress("test-suppress:Field", "test-suppress:SuppressedField", "field");
    }

    @Test
    void suppressedMethod() throws RuleException {
        verifySuppress("test-suppress:Method", "test-suppress:SuppressedMethod", "method");
    }

    @Test
    void suppressedMethodInPrimaryColumn() throws RuleException {
        verifySuppress("test-suppress:MethodInPrimaryColumn", "test-suppress:SuppressedMethodInPrimaryColumn", "method");
    }

    @Test
    void suppressedMethodInNonPrimaryColumn() throws RuleException {
        verifySuppress("test-suppress:MethodInNonPrimaryColumn", "test-suppress:SuppressedMethodInNonPrimaryColumn", "method");
    }

    private void verifySuppress(String constraintId, String conceptId, String column) throws RuleException {
        scanClasses(Suppress.class);
        assertThat(validateConstraint(constraintId).getStatus()).isEqualTo(SUCCESS);
        Result<Concept> supressedItems = applyConcept(conceptId);
        assertThat(supressedItems.getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        assertThat(supressedItems.getRows().size()).isEqualTo(1);
        Row row = supressedItems.getRows().get(0);
        JavaSuppressDescriptor suppressDescriptor = (JavaSuppressDescriptor) row.getColumns()
            .get(column)
            .getValue();
        assertThat(asList(suppressDescriptor.getSuppressIds()), hasItem(constraintId));
        store.commitTransaction();
    }
}
