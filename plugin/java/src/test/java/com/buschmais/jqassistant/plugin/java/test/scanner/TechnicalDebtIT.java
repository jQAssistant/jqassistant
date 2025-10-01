package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.WithoutTechnicalDebt;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static org.assertj.core.api.Assertions.assertThat;

class TechnicalDebtIT extends AbstractJavaPluginIT {

    @Test
    void technicalDebtAnnotationTest() throws RuleException {
        scanClasses(TechnicalDebtExample.class);
        Result<Constraint> constraintResult = validateConstraint("java:TechnicalDebt");
        assertThat(constraintResult.getStatus()).isEqualTo(FAILURE);
        store.beginTransaction();

        assertThat(constraintResult.getRows().size()).isEqualTo(4);

        assertThat(constraintResult.getRows().get(0).getColumns().get("Priority").getLabel()).isEqualTo("0");
        assertThat(constraintResult.getRows().get(0).getColumns().get("Type").getLabel()).isEqualTo("com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample");
        assertThat(constraintResult.getRows().get(0).getColumns().get("Member").getLabel()).isEqualTo("debtField");
        assertThat(constraintResult.getRows().get(0).getColumns().get("Description").getLabel()).isEqualTo("technicalDebt:Field with debts");
        assertThat(constraintResult.getRows().get(0).getColumns().get("Issue").getLabel()).isEqualTo("859");

        assertThat(constraintResult.getRows().get(1).getColumns().get("Priority").getLabel()).isEqualTo("1");
        assertThat(constraintResult.getRows().get(1).getColumns().get("Type").getLabel()).isEqualTo("com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample");
        assertThat(constraintResult.getRows().get(1).getColumns().get("Member").getLabel()).isEqualTo("");
        assertThat(constraintResult.getRows().get(1).getColumns().get("Description").getLabel()).isEqualTo("technicalDebt:Class with debts");
        assertThat(constraintResult.getRows().get(1).getColumns().get("Issue").getLabel()).isEqualTo("284");

        assertThat(constraintResult.getRows().get(2).getColumns().get("Priority").getLabel()).isEqualTo("1");
        assertThat(constraintResult.getRows().get(2).getColumns().get("Type").getLabel()).isEqualTo("com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample");
        assertThat(constraintResult.getRows().get(2).getColumns().get("Member").getLabel()).isEqualTo("");
        assertThat(constraintResult.getRows().get(2).getColumns().get("Description").getLabel()).isEqualTo("technicalDebt:Class with defaults");
        assertThat(constraintResult.getRows().get(2).getColumns().get("Issue").getLabel()).isEqualTo("");

        assertThat(constraintResult.getRows().get(3).getColumns().get("Priority").getLabel()).isEqualTo("2");
        assertThat(constraintResult.getRows().get(3).getColumns().get("Type").getLabel()).isEqualTo("com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample");
        assertThat(constraintResult.getRows().get(3).getColumns().get("Member").getLabel()).isEqualTo("doSomething");
        assertThat(constraintResult.getRows().get(3).getColumns().get("Description").getLabel()).isEqualTo("technicalDebt:Method with debts");
        assertThat(constraintResult.getRows().get(3).getColumns().get("Issue").getLabel()).isEqualTo("3");

        store.commitTransaction();
    }

    @Test
    void withoutTechnicalDebtTest() throws RuleException {
        scanClasses(WithoutTechnicalDebt.class);
        Result<Constraint> constraintResult = validateConstraint("java:TechnicalDebt");
        store.beginTransaction();
        assertThat(constraintResult.getRows().size()).isEqualTo(0);
        store.commitTransaction();
    }

}
