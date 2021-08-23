package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
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
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows).hasSize(1);
        Map<String, Object> row = rows.get(0);
        assertThat(row.get("Artifact")).isNotNull().isInstanceOf(JavaArtifactFileDescriptor.class);
        assertThat(row.get("GeneratedTypes")).isNotNull().isEqualTo(1l);
    }
}
