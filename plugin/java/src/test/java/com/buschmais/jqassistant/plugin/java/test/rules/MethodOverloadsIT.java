package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

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

    /**
     * Verifies the uniqueness of concept "java:MethodOverloads" with keeping existing properties.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void methodOverloadsUnique() throws Exception {
        scanClasses(InterfaceType.class);
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (i:Interface)-[:DECLARES]->(m1:Method), (i)-[:DECLARES]->(m2:Method) WHERE m1 <> m2 AND m1.name = m2.name AND m1.signature <> m2.signature AND m1.signature='void doSomething(int)' MERGE (m1)-[r:OVERLOADS {prop: 'value'}]->(m2) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (i:Interface)-[:DECLARES]->(m1:Method), (i)-[:DECLARES]->(m2:Method) WHERE m1 <> m2 AND m1.name = m2.name AND m1.signature <> m2.signature AND m1.signature='void doSomething(java.lang.String)' MERGE (m1)-[r:OVERLOADS]->(m2) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("OVERLOADS", 2);
        store.commitTransaction();
        assertThat(applyConcept("java:MethodOverloads").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("OVERLOADS", 2);
        store.commitTransaction();
    }

    /**
     * Verifies a unique relation with property. An existing transaction is assumed.
     * @param relationName The name of the relation.
     * @param total The total of relations with the given name.
     */
    private void verifyUniqueRelation(String relationName, int total) {
    	assertThat(query("MATCH ()-[r:" + relationName + " {prop: 'value'}]->() RETURN r").getColumn("r").size(), equalTo(1));
    	assertThat(query("MATCH ()-[r:" + relationName + "]->() RETURN r").getColumn("r").size(), equalTo(total));
    }
}
