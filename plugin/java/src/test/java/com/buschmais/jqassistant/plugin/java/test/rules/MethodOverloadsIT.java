package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InterfaceType;

/**
 * Tests for the concept java:MethodOverloads.
 */
public class MethodOverloadsIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:MethodOverloads".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void methodOverloads() throws Exception {
        scanClasses(InterfaceType.class);
        assertThat(applyConcept("java:MethodOverloads").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult result = query("MATCH (method:Method)-[:OVERLOADS]->(otherMethod:Method) RETURN method, otherMethod ORDER BY method.signature");
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(2));
        Map<String, Object> row0 = rows.get(0);
        assertThat((MethodDescriptor) row0.get("method"), methodDescriptor(InterfaceType.class, "doSomething", int.class));
        assertThat((MethodDescriptor) row0.get("otherMethod"), methodDescriptor(InterfaceType.class, "doSomething", String.class));
        Map<String, Object> row1 = rows.get(1);
        assertThat((MethodDescriptor) row1.get("method"), methodDescriptor(InterfaceType.class, "doSomething", String.class));
        assertThat((MethodDescriptor) row1.get("otherMethod"), methodDescriptor(InterfaceType.class, "doSomething", int.class));
        store.commitTransaction();
    }
}
