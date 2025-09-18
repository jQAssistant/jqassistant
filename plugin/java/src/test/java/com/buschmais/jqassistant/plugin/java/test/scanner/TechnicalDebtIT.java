package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.technicaldebt.TechnicalDebtExample;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

class TechnicalDebtIT extends AbstractJavaPluginIT {

    @Test
    void technicalDebtAnnotationTest() throws RuleException {
        scanClasses(TechnicalDebtExample.class); //
        Result<Constraint> constraintResult = validateConstraint("java:TechnicalDebtTest"); // simples Constraint failed mit technicalDebt Klasse, succeeded aber mit Suppress-Klasse
        assertThat(constraintResult.getStatus()).isEqualTo(SUCCESS);
/*
        store.beginTransaction();
        assertThat(constraintResult.getRows().get(0).getColumns().get("Priority").equals("LOW"));
        assertThat(constraintResult.getRows().size()).isEqualTo(2);
        System.out.println( constraintResult.getRows().get(0).getColumns().get("issue").toString());
        assertThat(constraintResult.getRows().get(0).getColumns().get("value").equals("technicalDebt:Method"));
        store.commitTransaction();
        assertThat(constraintResult.getStatus()).isEqualTo(SUCCESS);
*/
    }

}
