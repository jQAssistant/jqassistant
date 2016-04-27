package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InterfaceType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.SubClassType;

/**
 * Tests for the concept java:MethodOverrides.
 */
public class MethodOverridesIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:MethodOverrides" for a class implementing an
     * interface.
     * 
     * @throws IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void methodOverrides() throws Exception {
        scanClasses(ClassType.class, InterfaceType.class);
        assertThat(applyConcept("java:MethodOverrides").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult result = query("MATCH (method:Method)-[:OVERRIDES]->(otherMethod:Method) RETURN method, otherMethod ORDER BY method.signature");
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(2));
        Map<String, Object> row0 = rows.get(0);
        assertThat((MethodDescriptor) row0.get("method"), methodDescriptor(ClassType.class, "doSomething", int.class));
        assertThat((MethodDescriptor) row0.get("otherMethod"), methodDescriptor(InterfaceType.class, "doSomething", int.class));
        Map<String, Object> row1 = rows.get(1);
        assertThat((MethodDescriptor) row1.get("method"), methodDescriptor(ClassType.class, "doSomething", String.class));
        assertThat((MethodDescriptor) row1.get("otherMethod"), methodDescriptor(InterfaceType.class, "doSomething", String.class));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:MethodOverrides" for a class implementing an
     * interface.
     * 
     * @throws IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void methodOverridesSubClass() throws Exception {
        scanClasses(ClassType.class, SubClassType.class);
        assertThat(applyConcept("java:MethodOverrides").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult result = query("MATCH (method:Method)-[:OVERRIDES]->(otherMethod:Method) RETURN method, otherMethod ORDER BY method.signature");
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(3));
        Map<String, Object> row0 = rows.get(0);
        assertThat((MethodDescriptor) row0.get("method"), constructorDescriptor(SubClassType.class));
        assertThat((MethodDescriptor) row0.get("otherMethod"), constructorDescriptor(ClassType.class));
        Map<String, Object> row1 = rows.get(1);
        assertThat((MethodDescriptor) row1.get("method"), methodDescriptor(SubClassType.class, "doSomething", int.class));
        assertThat((MethodDescriptor) row1.get("otherMethod"), methodDescriptor(ClassType.class, "doSomething", int.class));
        Map<String, Object> row2 = rows.get(2);
        assertThat((MethodDescriptor) row2.get("method"), methodDescriptor(SubClassType.class, "doSomething", String.class));
        assertThat((MethodDescriptor) row2.get("otherMethod"), methodDescriptor(ClassType.class, "doSomething", String.class));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "java:MethodOverrides" with keeping existing properties.
     * 
     * @throws IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void methodOverridesUnique() throws Exception {
    	scanClasses(ClassType.class, InterfaceType.class);
        Map<String, Object> params = MapBuilder.<String, Object> create("class", ClassType.class.getName()).put("interface", InterfaceType.class.getName()).get();
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (t1:Type)-[:DECLARES]->(m1:Method), (t2:Type)-[:DECLARES]->(m2:Method) WHERE t1.fqn={class} AND t2.fqn={interface} AND m1.name = m2.name AND m1.signature = m2.signature AND m1.signature='void doSomething(int)' MERGE (m1)-[r:OVERRIDES {prop: 'value'}]->(m2) RETURN r", params).getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (t1:Type)-[:DECLARES]->(m1:Method), (t2:Type)-[:DECLARES]->(m2:Method) WHERE t1.fqn={class} AND t2.fqn={interface} AND m1.name = m2.name AND m1.signature = m2.signature AND m1.signature='void doSomething(java.lang.String)' MERGE (m1)-[r:OVERRIDES]->(m2) RETURN r", params).getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("OVERRIDES", 2);
        store.commitTransaction();
        assertThat(applyConcept("java:MethodOverrides").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("OVERRIDES", 2);
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
