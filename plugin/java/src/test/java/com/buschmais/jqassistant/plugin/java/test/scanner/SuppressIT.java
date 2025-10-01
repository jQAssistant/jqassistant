package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.JavaSuppressDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress.Suppress;

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
        Result<Constraint> constraintResult = validateConstraint("suppress:SuppressAnnotationMustNotBeScanned");
        assertThat(constraintResult.getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        assertThat(constraintResult.getRows().size()).isEqualTo(0);
        store.commitTransaction();
    }

    @Test
    void suppressedClass() throws RuleException {
        verifySuppress("suppress:Class", "suppress:SuppressedClass", "class");
    }

    @Test
    void suppressedField() throws RuleException {

        scanClasses(Suppress.class);
        Result<Constraint> result = validateConstraint("suppress:Field");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(2); // two not suppressed values due to expired and no expirationDates

        Result<Concept> supressedItems = applyConcept("suppress:SuppressedField");
        store.beginTransaction();
        assertThat(supressedItems.getStatus()).isEqualTo(SUCCESS);
        assertThat(supressedItems.getRows()
            .size()).isEqualTo(3);
        for (Row row : supressedItems.getRows()) {
            JavaSuppressDescriptor suppressDescriptor = (JavaSuppressDescriptor) row.getColumns()
                .get("field")
                .getValue();
            assertThat(asList(suppressDescriptor.getSuppressIds()), hasItem("suppress:Field"));
        }
        store.commitTransaction();
    }

    @Test
    void expirationDateShouldNotBeTooFarInTheFuture() throws RuleException {
        scanClasses(Suppress.class);
        Result<Constraint> result = validateConstraint("suppress:ExpirationDateShouldNotBeTooFarInTheFuture");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(3);
    }

    @Test
    void expirationDatesShouldNotBeInThePast() throws RuleException {
        scanClasses(Suppress.class);
        Result<Constraint> result = validateConstraint("suppress:ExpirationDatesShouldNotBeNullOrInThePast");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(2);
    }

    @Test
    void suppressExpiresInLessThanOneMonth() throws RuleException {
        scanClasses(Suppress.class);
        LocalDate dateInTwoWeeks = LocalDate.now()
            .plusWeeks(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        query("MATCH (n:Java:jQASuppress {name: 'expiredValue'}) SET n.suppressUntil = '" + dateInTwoWeeks.format(formatter) + "' RETURN n");
        Result<Constraint> result = validateConstraint("suppress:suppressExpiresInLessThanOneMonth");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(1);
    }

    @Test
    void suppressedMethod() throws RuleException {
        verifySuppress("suppress:Method", "suppress:SuppressedMethod", "method");
    }

    @Test
    void suppressedMethodInPrimaryColumn() throws RuleException {
        verifySuppress("suppress:MethodInPrimaryColumn", "suppress:SuppressedMethodInPrimaryColumn", "method");
    }

    @Test
    void suppressedMethodInNonPrimaryColumn() throws RuleException {
        verifySuppress("suppress:MethodInNonPrimaryColumn", "suppress:SuppressedMethodInNonPrimaryColumn", "method");
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
