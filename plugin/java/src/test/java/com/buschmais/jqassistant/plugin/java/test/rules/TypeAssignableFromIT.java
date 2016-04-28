package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InterfaceType;

/**
 * Tests for the concept java:TypeAssignableFrom.
 */
public class TypeAssignableFromIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:AssignableFrom".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void assignableFrom() throws Exception {
        scanClasses(ClassType.class);
        assertThat(applyConcept("java:TypeAssignableFrom").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (types:Type)<-[:ASSIGNABLE_FROM]-(assignableType) RETURN assignableType").getColumn("assignableType"),
                allOf(hasItem(typeDescriptor(ClassType.class)), hasItem(typeDescriptor(InterfaceType.class)), hasItem(typeDescriptor(Object.class))));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "java:AssignableFrom" with keeping existing properties.
     * 
     * @throws IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void assignableFromUnique() throws Exception {
      scanClasses(ClassType.class);
      store.beginTransaction();
      // create existing relations with and without properties
      assertThat(query("MATCH (c:Class {name: 'ClassType'}) MERGE (c)-[r:ASSIGNABLE_FROM {prop: 'value'}]->(c) RETURN r").getColumn("r").size(), equalTo(1));
      assertThat(query("MATCH (i:Type {name: 'InterfaceType'}) MERGE (i)-[r:ASSIGNABLE_FROM]->(i) RETURN r").getColumn("r").size(), equalTo(1));
      assertThat(query("MATCH (c:Class {name: 'ClassType'}), (i:Type {name: 'InterfaceType'}) MERGE (i)-[r:ASSIGNABLE_FROM {prop: 'value'}]->(c) RETURN r").getColumn("r").size(), equalTo(1));
      assertThat(query("MATCH (c:Class {name: 'ClassType'}), (o:Type {name: 'Object'}) MERGE (o)-[r:ASSIGNABLE_FROM]->(c) RETURN r").getColumn("r").size(), equalTo(1));
      verifyUniqueRelation("ASSIGNABLE_FROM", 4);
      store.commitTransaction();
      assertThat(applyConcept("java:TypeAssignableFrom").getStatus(), equalTo(SUCCESS));
      store.beginTransaction();
      verifyUniqueRelation("ASSIGNABLE_FROM", 8);
      store.commitTransaction();
    }

    /**
     * Verifies a unique relation with property. An existing transaction is assumed.
     * @param relationName The name of the relation.
     * @param total The total of relations with the given name.
     */
    private void verifyUniqueRelation(String relationName, int total) {
    	assertThat(query("MATCH ()-[r:" + relationName + " {prop: 'value'}]->() RETURN r").getColumn("r").size(), equalTo(2));
    	assertThat(query("MATCH (a)-[r:" + relationName + "]->(b) RETURN a.name + '-' + b.name as r").getColumn("r").size(), equalTo(total));
    }
}
