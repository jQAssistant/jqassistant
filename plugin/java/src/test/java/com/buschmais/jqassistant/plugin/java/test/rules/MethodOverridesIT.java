package com.buschmais.jqassistant.plugin.java.test.rules;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.InterfaceType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.SubClassType;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Tests for the concept java:MethodOverrides.
 */
public class MethodOverridesIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:MethodOverrides" for a hierarchy of an interface,
     * class and sub-class.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void methodOverrides() throws Exception {
        scanClasses(SubClassType.class, ClassType.class, InterfaceType.class);
        assertThat(applyConcept("java:MethodOverrides").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult result = query(
                "MATCH (type:Type)-[:DECLARES]->(method:Method)-[:OVERRIDES]->(superMethod:Method)<-[:DECLARES]-(superType:Type) RETURN method, superMethod ORDER BY type.fqn, method.signature");
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(7));
        verifyOverridesMethod(rows.get(0), ClassType.class, InterfaceType.class, "doSomething", boolean.class);
        verifyOverridesMethod(rows.get(1), ClassType.class, InterfaceType.class, "doSomething", int.class);
        verifyOverridesMethod(rows.get(2), ClassType.class, InterfaceType.class, "doSomething", String.class);
        verifyOverridesConstructor(rows.get(3), SubClassType.class, ClassType.class);
        Map<String, Object> row1 = rows.get(4);
        // subClassMethod implicitly created by method invocation
        MethodDescriptor subClassMethod = (MethodDescriptor) row1.get("method");
        MethodDescriptor superClassMethod = (MethodDescriptor) row1.get("superMethod");
        assertThat(subClassMethod.getSignature(), equalTo(superClassMethod.getSignature()));
        assertThat(subClassMethod.getName(), nullValue());
        assertThat(subClassMethod.getVisibility(), nullValue());
        verifyOverridesMethod(rows.get(5), SubClassType.class, ClassType.class, "doSomething", int.class);
        verifyOverridesMethod(rows.get(6), SubClassType.class, ClassType.class, "doSomething", String.class);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "java:MethodOverrides" with keeping
     * existing properties.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void methodOverridesUnique() throws Exception {
        scanClasses(ClassType.class, InterfaceType.class);
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("class", ClassType.class.getName())
                .entry("interface", InterfaceType.class.getName()).build();
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query(
                "MATCH (t1:Type)-[:DECLARES]->(m1:Method), (t2:Type)-[:DECLARES]->(m2:Method) WHERE t1.fqn={class} AND t2.fqn={interface} AND m1.name = m2.name AND m1.signature = m2.signature AND m1.signature='void doSomething(int)' MERGE (m1)-[r:OVERRIDES {prop: 'value'}]->(m2) RETURN r",
                params).getColumn("r").size(), equalTo(1));
        assertThat(query(
                "MATCH (t1:Type)-[:DECLARES]->(m1:Method), (t2:Type)-[:DECLARES]->(m2:Method) WHERE t1.fqn={class} AND t2.fqn={interface} AND m1.name = m2.name AND m1.signature = m2.signature AND m1.signature='void doSomething(java.lang.String)' MERGE (m1)-[r:OVERRIDES]->(m2) RETURN r",
                params).getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("OVERRIDES", 2);
        store.commitTransaction();
        assertThat(applyConcept("java:MethodOverrides").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("OVERRIDES", 3);
        store.commitTransaction();
    }

    private void verifyOverridesMethod(Map<String, Object> row, Class<?> type, Class<?> superType, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        assertThat((MethodDescriptor) row.get("method"), methodDescriptor(type, methodName, parameterTypes));
        assertThat((MethodDescriptor) row.get("superMethod"), methodDescriptor(superType, methodName, parameterTypes));
    }

    private void verifyOverridesConstructor(Map<String, Object> row, Class<?> type, Class<?> superType, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        assertThat((MethodDescriptor) row.get("method"), constructorDescriptor(type, parameterTypes));
        assertThat((MethodDescriptor) row.get("superMethod"), constructorDescriptor(superType, parameterTypes));
    }

    /**
     * Verifies a unique relation with property. An existing transaction is assumed.
     *
     * @param relationName
     *            The name of the relation.
     * @param total
     *            The total of relations with the given name.
     */
    private void verifyUniqueRelation(String relationName, int total) {
        assertThat(query("MATCH ()-[r:" + relationName + " {prop: 'value'}]->() RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH ()-[r:" + relationName + "]->() RETURN r").getColumn("r").size(), equalTo(total));
    }
}
