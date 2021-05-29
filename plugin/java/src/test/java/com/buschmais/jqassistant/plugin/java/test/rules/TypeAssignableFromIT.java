package com.buschmais.jqassistant.plugin.java.test.rules;

import java.io.IOException;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.AbstractClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.InterfaceType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.inheritance.SubClassType;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;

/**
 * Tests for the concept java:TypeAssignableFrom.
 */
public class TypeAssignableFromIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:AssignableFrom".
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void assignableFrom() throws RuleException {
        scanClasses(SubClassType.class, AbstractClassType.class, InterfaceType.class);
        assertThat(applyConcept("java:TypeAssignableFrom").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (types:Type)<-[:ASSIGNABLE_FROM]-(assignableType) RETURN assignableType").getColumn("assignableType"),
                hasItems(typeDescriptor(SubClassType.class), typeDescriptor(AbstractClassType.class), typeDescriptor(InterfaceType.class),
                        typeDescriptor(Object.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "java:AssignableFrom" with keeping
     * existing properties.
     *
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void assignableFromUnique() throws RuleException {
        scanClasses(AbstractClassType.class);
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (c:Class {name: 'AbstractClassType'}) MERGE (c)-[r:ASSIGNABLE_FROM {prop: 'value'}]->(c) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (i:Type {name: 'InterfaceType'}) MERGE (i)-[r:ASSIGNABLE_FROM]->(i) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (c:Class {name: 'AbstractClassType'}), (i:Type {name: 'InterfaceType'}) MERGE (i)-[r:ASSIGNABLE_FROM {prop: 'value'}]->(c) RETURN r")
                .getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (c:Class {name: 'AbstractClassType'}), (o:Type {name: 'Object'}) MERGE (o)-[r:ASSIGNABLE_FROM]->(c) RETURN r").getColumn("r").size(),
                equalTo(1));
        verifyUniqueRelation("ASSIGNABLE_FROM", 4);
        store.commitTransaction();
        assertThat(applyConcept("java:TypeAssignableFrom").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("ASSIGNABLE_FROM", 7);
        store.commitTransaction();
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
        assertThat(query("MATCH ()-[r:" + relationName + " {prop: 'value'}]->() RETURN r").getColumn("r").size(), equalTo(2));
        assertThat(query("MATCH (a)-[r:" + relationName + "]->(b) RETURN a.name + '-' + b.name as r").getColumn("r").size(), equalTo(total));
    }
}
