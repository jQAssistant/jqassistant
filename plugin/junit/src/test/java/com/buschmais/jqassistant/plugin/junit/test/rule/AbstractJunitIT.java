package com.buschmais.jqassistant.plugin.junit.test.rule;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.report.Example;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

abstract class AbstractJunitIT extends AbstractJavaPluginIT {
    /**
     * Verifies if a IMPLEMENTED_BY relation exists between a test case and and test method.
     *
     * @param declaringType
     *            The class declaring the test method.
     * @param testcase
     *            The name of the test case.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    protected void verifyTestCaseImplementedByMethod(Class<?> declaringType, String testcase) throws NoSuchMethodException {
        assertThat(query("MATCH (testcase:TestCase)-[:DEFINED_BY]->(testclass:Type) WHERE testcase.name = '" +
                         testcase + "' RETURN testclass").getColumn("testclass"),
                   hasItem(typeDescriptor(Example.class)));
        assertThat(query("MATCH (testcase:TestCase)-[:IMPLEMENTED_BY]->(testmethod:Method) WHERE testcase.name = '" +
                         testcase + "' RETURN testmethod").getColumn("testmethod"),
                   hasItem(methodDescriptor(declaringType, testcase)));
    }

    /**
     * Verifies a unique relation with property. An existing transaction is assumed.
     * @param relationName The name of the relation.
     * @param total The total of relations with the given name.
     */
    protected void verifyUniqueRelation(String relationName, int total) {
        assertThat(query("MATCH ()-[r:" + relationName + " {prop: 'value'}]->() RETURN r").getColumn("r").size(), equalTo(1));
        assertThat(query("MATCH ()-[r:" + relationName + "]->() RETURN r").getColumn("r").size(), equalTo(total));
    }

    protected void verifyRelationForImplementedBy(String testcase, String methodName, String clazzName) {
        String query = format("MATCH (tc:TestCase { name: '%s' })-[:IMPLEMENTED_BY]->" +
                       "(m:Method { name: '%s' } )<-[:DECLARES]-(c:Class { name: '%s' })" +
                       " RETURN tc, m, c",
                       testcase, methodName, clazzName);

        TestResult result = query(query);
        String reason = format("Relation (%s)-[:IMPLEMENTED_BY]->(%s)... not found.", testcase, methodName);
        assertThat(reason, result.getRows(), hasSize(1));
    }

    protected void verifyRelationForDefinedBy(String testcase, String clazzName) {
        String query = format("MATCH (tc:TestCase { name: '%s' })-[:DEFINED_BY]->" +
                              "(c:Class { name: '%s' })" +
                              " RETURN tc, c",
                              testcase, clazzName);

        TestResult result = query(query);
        String reason = format("Relation (%s)-[:DEFINED_BY]->(%s) not found.", testcase, clazzName);
        assertThat(reason, result.getRows(), hasSize(1));
    }
}
