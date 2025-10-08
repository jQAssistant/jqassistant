package com.buschmais.jqassistant.plugin.java.test.rules;

import java.time.LocalDate;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress.SuppressRules;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.WARNING;
import static org.assertj.core.api.Assertions.assertThat;

public class SuppressIT extends AbstractJavaPluginIT {

    @Test
    void suppressUntilWithMonthsLimit() throws RuleException {
        scanClasses(SuppressRules.class);
        Result<Constraint> result = validateConstraint("suppress:suppressUntilWithMonthsLimit");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(3);
        assertThat((result.getRows().get(0).getColumns().get("AnnotatedElement")).getValue()).isEqualTo("SuppressRules");
        assertThat((result.getRows().get(1).getColumns().get("AnnotatedElement")).getValue()).isEqualTo("suppressedValue");
        assertThat((result.getRows().get(2).getColumns().get("AnnotatedElement")).getValue()).isEqualTo("suppressedMethod");
    }

    @Test
    void suppressUntilMustNotBeInThePast() throws RuleException {
        scanClasses(SuppressRules.class);
        Result<Constraint> result = validateConstraint("suppress:suppressUntilMustNotBeInThePast");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(1);
        assertThat((result.getRows().get(0).getColumns().get("AnnotatedElement")).getValue()).isEqualTo("expiredValue");
    }

    @Test
    void suppressExpiresInLessThanOneMonth() throws RuleException {
        scanClasses(SuppressRules.class);
        LocalDate dateInTwoWeeks = LocalDate.now()
            .plusWeeks(2);
        query("MATCH (n:Java:jQASuppress {name: 'suppressedValue'}) SET n.suppressUntil = date('"+ dateInTwoWeeks + "') RETURN n");
        Result<Constraint> result = validateConstraint("suppress:suppressExpiresInLessThanOneMonth");
        assertThat(result.getStatus()).isEqualTo(WARNING);
        assertThat(result.getRows()
            .size()).isEqualTo(1);
        assertThat((result.getRows().get(0).getColumns().get("AnnotatedElement")).getValue()).isEqualTo("suppressedValue");
    }

    @Test
    void suppressFieldsMustProvideAReason() throws RuleException {
        scanClasses(SuppressRules.class);
        Result<Constraint> result = validateConstraint("suppress:suppressFieldsMustProvideAReason");
        assertThat(result.getStatus()).isEqualTo(FAILURE);
        assertThat(result.getRows()
            .size()).isEqualTo(3);

        assertThat((result.getRows().get(0).getColumns().get("AnnotatedElement")).getValue()).isEqualTo("expiredValue");
        assertThat((result.getRows().get(1).getColumns().get("AnnotatedElement")).getValue()).isEqualTo("suppressedValue");
        assertThat((result.getRows().get(2).getColumns().get("AnnotatedElement")).getValue()).isEqualTo("suppressedMethod");
    }
}
