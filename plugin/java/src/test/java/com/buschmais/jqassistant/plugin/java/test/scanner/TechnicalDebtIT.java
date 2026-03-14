package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
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
        verifyRow(constraintResult, 0, "0", "com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample",
            "java.lang.String debtField", "technicalDebt:Field with debts", "859");

        verifyRow(constraintResult, 1, "1", "com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample",
            "", "technicalDebt:Class with debts", "284");

        verifyRow(constraintResult, 2, "1", "com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample",
            "", "technicalDebt:Class with defaults", "");

        verifyRow(constraintResult, 3, "2", "com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample",
            "void doSomething()", "technicalDebt:Method with debts", "3");

        store.commitTransaction();
    }

    private static void verifyRow(Result<Constraint> constraintResult, int index, String expectedPriority, String expectedType, String expectedMemberSignature,
        String expectedDescription, String expectedIssue) {
        Row row = constraintResult.getRows()
            .get(index);
        assertThat(row.getColumns().get("Priority").getLabel()).isEqualTo(expectedPriority);
        assertThat(row.getColumns().get("Type").getLabel()).isEqualTo(expectedType);
        assertThat(row.getColumns().get("Member").getLabel()).isEqualTo(expectedMemberSignature);
        assertThat(row.getColumns().get("Description").getLabel()).isEqualTo(expectedDescription);
        assertThat(row.getColumns().get("Issue").getLabel()).isEqualTo(expectedIssue);
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
