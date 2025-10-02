package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.time.LocalDate;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.JavaSuppressDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress.Suppress;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress.SuppressRules;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

class SuppressIT extends AbstractJavaPluginIT {

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

    @Test
    void suppressUntilWithMonthsLimit() throws RuleException {
        scanClasses(SuppressRules.class);
        Result<Constraint> result = validateConstraint("suppress:suppressUntilWithMonthsLimit");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(3);
    }

    @Test
    void suppressUntilMustNotBeInThePast() throws RuleException {
        scanClasses(SuppressRules.class);
        Result<Constraint> result = validateConstraint("suppress:suppressUntilMustNotBeInThePast");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(1);
    }

    @Test
    void suppressExpiresInLessThanOneMonth() throws RuleException {
        scanClasses(SuppressRules.class);
        LocalDate dateInTwoWeeks = LocalDate.now()
            .plusWeeks(2);
        query("MATCH (n:Java:jQASuppress {name: 'suppressedValue'}) SET n.suppressUntil = '" + dateInTwoWeeks + "' RETURN n");
        Result<Constraint> result = validateConstraint("suppress:suppressExpiresInLessThanOneMonth");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(1);
    }

    @Test
    void suppressFieldsMustProvideAReason() throws RuleException {
        scanClasses(SuppressRules.class);
        Result<Constraint> result = validateConstraint("suppress:suppressFieldsMustProvideAReason");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(3);
    }

}
