package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.lombok.Person;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

class LombokIT extends AbstractJavaPluginIT {

    @Test
    void generatedLombokType() throws RuleException {
        scanClasses(Person.class, Person.PersonBuilder.class);

        Result<Concept> result = applyConcept("java:GeneratedLombokType");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Row> rows = result.getRows();
        assertThat(rows).hasSize(1);

        Row row = rows.get(0);
        Column<?> generatedType = row.getColumns()
            .get("GeneratedTypes");
        assertThat(generatedType).isNotNull();
        assertThat(generatedType.getValue()).isEqualTo(1L);
    }

    @Test
    void generatedType() throws RuleException {
        scanClasses(Person.class, Person.PersonBuilder.class);

        Result<Concept> result = applyConcept("java:GeneratedType");
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
    }
}
