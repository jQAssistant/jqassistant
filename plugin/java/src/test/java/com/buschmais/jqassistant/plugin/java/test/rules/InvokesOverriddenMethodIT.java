package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.java.api.model.InvokesDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.a.ExceptionType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.classpath.resolve.b.DependentType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InterfaceType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InvokeClient;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.SubClassType;

/**
 * Tests for the concept java:InvokeOverriddenMethod.
 */
public class InvokesOverriddenMethodIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:InvokeOverriddenMethod" for a class
     * implementing an interface.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void invokeInterfaceMethod() throws Exception {
        scanClasses(ClassType.class, InterfaceType.class, InvokeClient.class);
        assertThat(applyConcept("java:InvokesOverriddenMethod").getStatus(), Matchers.equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> classes = query(
                "MATCH (client:Type)-[:DECLARES]->(clientMethod:Method)-[:INVOKES]->(invokedMethod:Method)<-[:DECLARES]-(type:Type) WHERE client.name='InvokeClient' and clientMethod.name='invokeInterfaceTypeMethod' RETURN type")
                .getColumn("type");
        assertThat(classes, hasItem(TypeDescriptorMatcher.typeDescriptor(InterfaceType.class)));
        assertThat(classes, hasItem(TypeDescriptorMatcher.typeDescriptor(ClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "java:InvokeOverriddenMethod" for a sub class
     * extending a class.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void invokeClassMethod() throws Exception {
        scanClasses(ClassType.class, SubClassType.class, InvokeClient.class);
        assertThat(applyConcept("java:InvokesOverriddenMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<Object> classes = query(
                "MATCH (client:Type)-[:DECLARES]->(clientMethod:Method)-[:INVOKES]->(invokedMethod:Method)<-[:DECLARES]-(type:Type) WHERE client.name='InvokeClient' and clientMethod.name='invokeClassTypeMethod' RETURN type")
                .getColumn("type");
        assertThat(classes, hasItem(TypeDescriptorMatcher.typeDescriptor(ClassType.class)));
        assertThat(classes, hasItem(TypeDescriptorMatcher.typeDescriptor(SubClassType.class)));
        store.commitTransaction();
    }

    /**
     * Verifies that the concept "java:InvokeOverriddenMethod" keeps line number
     * information.
     *
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void lineNumbers() throws Exception {
        scanClasses(ClassType.class, InterfaceType.class, InvokeClient.class);
        assertThat(applyConcept("java:InvokesOverriddenMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        List<InvokesDescriptor> interfaceInvocations = query(
                "MATCH (client:Type)-[:DECLARES]->(clientMethod:Method)-[invocation:INVOKES]->(invokedMethod:Method)<-[:DECLARES]-(type:Type) "
                        + "WHERE client.name='InvokeClient' and clientMethod.name='invokeInterfaceTypeMethod' and type.name='InterfaceType'"
                        + "RETURN invocation ORDER BY invocation.lineNumber").getColumn("invocation");
        assertThat(interfaceInvocations.size(), equalTo(2));
        InvokesDescriptor interfaceInvocation1 = interfaceInvocations.get(0);
        InvokesDescriptor interfaceInvocation2 = interfaceInvocations.get(1);
        assertThat(interfaceInvocation1.getLineNumber(), notNullValue());
        assertThat(interfaceInvocation2.getLineNumber(), notNullValue());
        assertThat(interfaceInvocation1.getLineNumber(), not(equalTo(interfaceInvocation2.getLineNumber())));
        List<InvokesDescriptor> classInvocations = query(
                "MATCH (client:Type)-[:DECLARES]->(clientMethod:Method)-[invocation:INVOKES]->(invokedMethod:Method)<-[:DECLARES]-(type:Type) "
                        + "WHERE client.name='InvokeClient' and clientMethod.name='invokeInterfaceTypeMethod' and type.name='ClassType'"
                        + "RETURN invocation ORDER BY invocation.lineNumber").getColumn("invocation");
        assertThat(classInvocations.size(), equalTo(2));
        InvokesDescriptor classInvocation1 = classInvocations.get(0);
        InvokesDescriptor classInvocation2 = classInvocations.get(1);
        assertThat(classInvocation1.getLineNumber(), equalTo(interfaceInvocation1.getLineNumber()));
        assertThat(classInvocation2.getLineNumber(), equalTo(interfaceInvocation2.getLineNumber()));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "java:InvokeOverriddenMethod" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveWritesUniqueSameLine() throws Exception {
        scanClasses(ClassType.class, InterfaceType.class, InvokeClient.class);
        assertThat(applyConcept("java:MethodOverrides").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and correct line number
        assertThat(query("MATCH (m1:Method {name: 'invokeInterfaceTypeMethod'})-[:INVOKES {lineNumber: 9}]->(:Method)<-[:OVERRIDES]-(m2:Method {name: 'doSomething'}) MERGE (m1)-[r:INVOKES {lineNumber: 9, prop: 'value'}]->(m2) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("INVOKES", 7);
        store.commitTransaction();
        assertThat(applyConcept("java:InvokesOverriddenMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("INVOKES", 8);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "java:InvokeOverriddenMethod" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveWritesUniqueDifferentLine() throws Exception {
        scanClasses(ClassType.class, InterfaceType.class, InvokeClient.class);
        assertThat(applyConcept("java:MethodOverrides").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and different line number
        assertThat(query("MATCH (m1:Method {name: 'invokeInterfaceTypeMethod'})-[:INVOKES {lineNumber: 9}]->(:Method)<-[:OVERRIDES]-(m2:Method {name: 'doSomething'}) MERGE (m1)-[r:INVOKES {lineNumber: 90, prop: 'value'}]->(m2) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("INVOKES", 7);
        store.commitTransaction();
        assertThat(applyConcept("java:InvokesOverriddenMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("INVOKES", 9);
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of the concept "java:InvokeOverriddenMethod" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws AnalysisException
     *             If the test fails.
     */
    @Test
    public void resolveWritesUniqueWithoutLine() throws Exception {
        scanClasses(ClassType.class, InterfaceType.class, InvokeClient.class);
        assertThat(applyConcept("java:MethodOverrides").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        // create existing relations with properties and without line number
        assertThat(query("MATCH (m1:Method {name: 'invokeInterfaceTypeMethod'})-[:INVOKES {lineNumber: 9}]->(:Method)<-[:OVERRIDES]-(m2:Method {name: 'doSomething'}) MERGE (m1)-[r:INVOKES {prop: 'value'}]->(m2) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("INVOKES", 7);
        store.commitTransaction();
        assertThat(applyConcept("java:InvokesOverriddenMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("INVOKES", 9);
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
