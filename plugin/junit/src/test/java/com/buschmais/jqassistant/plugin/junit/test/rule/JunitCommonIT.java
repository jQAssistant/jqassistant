package com.buschmais.jqassistant.plugin.junit.test.rule;

import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.report.AbstractExample;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.report.Example;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JunitCommonIT extends AbstractJunitIT {

    /**
     * Verifies the concept "junit:TestCaseImplementedByMethod".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testCaseImplementedByMethod() throws Exception {
        scanClasses(AbstractExample.class, Example.class);
        scanClassPathResource(JunitScope.TESTREPORTS, "/TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml");
        assertThat(applyConcept("junit:TestCaseImplementedByMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyTestCaseImplementedByMethod(Example.class, "success");
        verifyTestCaseImplementedByMethod(AbstractExample.class, "inherited");
        verifyTestCaseImplementedByMethod(Example.class, "failure");
        verifyTestCaseImplementedByMethod(Example.class, "error");
        verifyTestCaseImplementedByMethod(Example.class, "skipped");
        store.commitTransaction();
    }

    /**
     * Verifies the uniqueness of concept "junit:TestCaseImplementedByMethod" with keeping existing properties.
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testCaseImplementedByMethodUnique() throws Exception {
        scanClasses(Example.class);
        scanClassPathResource(JunitScope.TESTREPORTS, "/TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml");
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(query("MATCH (t:TestCase {name: 'success'}), (m:Method {name: 'success'}) MERGE (t)-[r:IMPLEMENTED_BY {prop: 'value'}]->(m) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (t:TestCase {name: 'failure'}), (m:Method {name: 'failure'}) MERGE (t)-[r:IMPLEMENTED_BY]->(m) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (t:TestCase {name: 'success'}), (c:Type {name: 'Example'}) MERGE (t)-[r:DEFINED_BY {prop: 'value'}]->(c) RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH (t:TestCase {name: 'failure'}), (c:Type {name: 'Example'}) MERGE (t)-[r:DEFINED_BY]->(c) RETURN r").getColumn("r").size(), equalTo(1));
        verifyUniqueRelation("IMPLEMENTED_BY", 2);
        verifyUniqueRelation("DEFINED_BY", 2);
        store.commitTransaction();
        assertThat(applyConcept("junit:TestCaseImplementedByMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("IMPLEMENTED_BY", 4);
        verifyUniqueRelation("DEFINED_BY", 5);
        store.commitTransaction();
    }
}
