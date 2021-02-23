package com.buschmais.jqassistant.plugin.java.test.rules;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.packageannotation.PackageClass;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests for the concept java:PackageAnnotatedBy.
 */
public class PackageAnnotatedByIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:PackageAnnotatedBy".
     */
    @Test
    public void packageAnnotatedBy() throws Exception {
        scanClassesAndPackages(PackageClass.class);
        Result<Concept> result = applyConcept("java:PackageAnnotatedBy");
        assertThat(result.getStatus(), equalTo(SUCCESS));
        assertThat(result.getRows().size(), equalTo(1));

        store.beginTransaction();
        assertThat(query("MATCH (p:Package)-[:ANNOTATED_BY]->(:Annotation) RETURN p").getRows().size(), equalTo(1));
        store.commitTransaction();
    }

    private void scanClassesAndPackages(Class<?> clazz) {
        Map<String, Object> pluginProps = new HashMap<>();
        pluginProps.put("file.include", "/" + clazz.getPackage().getName().replace(".", "/") + "/**");
        getScanner(pluginProps).scan(getClassesDirectory(clazz), "/", JavaScope.CLASSPATH);
    }
}
