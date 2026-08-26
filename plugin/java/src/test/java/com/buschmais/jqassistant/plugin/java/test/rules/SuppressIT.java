package com.buschmais.jqassistant.plugin.java.test.rules;

import java.time.LocalDate;
import java.util.List;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress.SuppressRules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static org.assertj.core.api.Assertions.assertThat;

public class SuppressIT extends AbstractJavaPluginIT {

    @BeforeEach
    void init() {
        scanClasses(SuppressRules.class, SuppressRules.ClassWithoutReason.class);
    }

    @Test
    void suppressUntilWithMonthsLimit() throws RuleException {
        Result<Constraint> result = validateConstraint("java-suppress:SuppressUntilMustNotExceedMonthsLimit");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(3);

        store.beginTransaction();
        assertThat(((NamedDescriptor) result.getRows()
            .get(0)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("SuppressRules");
        assertThat(((NamedDescriptor) result.getRows()
            .get(1)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("suppressedValue");
        assertThat(((NamedDescriptor) result.getRows()
            .get(2)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("suppressedMethod");
        assertThat(result.getRows()
            .get(0)
            .getColumns()
            .get("ExpirationDate")
            .getValue()).isEqualTo(LocalDate.parse("2075-08-25"));
        assertThat(result.getRows()
            .get(1)
            .getColumns()
            .get("ExpirationDate")
            .getValue()).isEqualTo(LocalDate.parse("2075-06-04"));
        assertThat(result.getRows()
            .get(2)
            .getColumns()
            .get("ExpirationDate")
            .getValue()).isEqualTo(LocalDate.parse("2075-12-31"));
        store.commitTransaction();
    }

    @Test
    void suppressUntilMustNotBeInThePast() throws RuleException {
        Result<Constraint> result = validateConstraint("java-suppress:SuppressUntilMustNotBeInThePast");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        List<Row> rows = result.getRows();
        assertThat(rows.size()).isEqualTo(2);

        store.beginTransaction();
        assertThat(((NamedDescriptor) rows.get(0)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("SuppressRules$ClassWithoutReason");
        assertThat(((NamedDescriptor) rows.get(1)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("expiredValue");
        assertThat(rows.get(0)
            .getColumns()
            .get("ExpirationDate")
            .getValue()).isEqualTo(LocalDate.parse("2025-02-14"));
        assertThat(rows.get(1)
            .getColumns()
            .get("ExpirationDate")
            .getValue()).isEqualTo(LocalDate.parse("2024-08-25"));
        store.commitTransaction();
    }

    @Test
    void suppressExpiresInLessThanOneMonth() throws RuleException {
        LocalDate dateInTwoWeeks = LocalDate.now()
            .plusWeeks(2);
        query("MATCH (n:Java:jQASuppress{name: 'suppressedValue'})-[:HAS_SUPPRESSION]->(suppression:jQASuppression) SET suppression.until = date('"
            + dateInTwoWeeks + "')");
        Result<Constraint> result = validateConstraint("java-suppress:SuppressExpiresInLessThanMonthsLimit");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        List<Row> rows = result.getRows();
        assertThat(rows.size()).isEqualTo(1);

        store.beginTransaction();
        assertThat(((NamedDescriptor) result.getRows()
            .get(0)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("suppressedValue");
        assertThat(result.getRows()
            .get(0)
            .getColumns()
            .get("ExpirationDate")
            .getValue()).isEqualTo(dateInTwoWeeks);
        store.commitTransaction();
    }

    @Test
    void suppressFieldsMustProvideAReason() throws RuleException {
        Result<Constraint> result = validateConstraint("java-suppress:SuppressElementMustProvideAReason");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        List<Row> rows = result.getRows();
        assertThat(rows.size()).isEqualTo(4);

        store.beginTransaction();
        assertThat(((NamedDescriptor) rows.get(0)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("SuppressRules$ClassWithoutReason");
        assertThat(((NamedDescriptor) rows.get(1)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("expiredValue");
        assertThat(((NamedDescriptor) rows.get(2)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("suppressedValue");
        assertThat(((NamedDescriptor) rows.get(3)
            .getColumns()
            .get("Element")
            .getValue()).getName()).isEqualTo("suppressedMethod");
        store.commitTransaction();
    }
}
