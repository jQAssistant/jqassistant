package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.generated.Generated;
import com.buschmais.jqassistant.plugin.java.test.set.rules.generated.NotGenerated;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the concept java:GeneratedType.
 */
class GeneratedTypeIT extends AbstractJavaPluginIT {

    @Test
    void generatedTypes() throws Exception {
        scanClasses(Generated.class, NotGenerated.class);
        Result<Concept> result = applyConcept("java:GeneratedType"); // implicitly executes concept "test:GeneratedType"
        assertThat(result.getStatus()).isEqualTo(SUCCESS);
        List<Row> rows = result.getRows();
        assertThat(rows).hasSize(1);
        Map<String, Column<?>> row = rows.get(0).getColumns();
        Column artifactColumn = row.get("Artifact");
        assertThat(artifactColumn).isNotNull();
        assertThat(artifactColumn.getValue()).isInstanceOf(JavaArtifactFileDescriptor.class);
        Column generatedTypesColumn = row.get("GeneratedTypes");
        assertThat(generatedTypesColumn).isNotNull();
        assertThat(generatedTypesColumn.getValue()).isEqualTo(1l);
    }
}
