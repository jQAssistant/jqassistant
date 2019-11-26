package com.buschmais.jqassistant.plugin.junit.test.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.Assertions4Junit4;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.IgnoredTest;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.IgnoredTestWithMessage;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.report.AbstractExample;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.report.Example;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.Assertions4Junit5;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.DisabledTestWithMessage;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.DisabledTestWithoutMessage;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.analysis.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.containsInAnyOrder;
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

    /**
     * Verifies the concept "junit4:IgnoreWithoutMessage".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void ignoreWithoutMessage() throws Exception {
        scanClasses(IgnoredTest.class, IgnoredTestWithMessage.class, DisabledTestWithMessage.class,
                    DisabledTestWithoutMessage.class);
        assertThat(validateConstraint("junit:IgnoreWithoutMessage").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportPlugin.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit:IgnoreWithoutMessage")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(4));

        List<MethodDescriptor> methods = rows.stream()
                                             .map(map -> new Tuple<>("IgnoreWithoutMessage", map.get("IgnoreWithoutMessage")))
                                             .filter(tuple -> tuple.b() instanceof MethodDescriptor)
                                             .map(tuple -> MethodDescriptor.class.cast(tuple.b()))
                                             .collect(toList());

        List<TypeDescriptor> types = rows.stream()
                                         .map(map -> new Tuple<>("IgnoreWithoutMessage", map.get("IgnoreWithoutMessage")))
                                         .filter(tuple -> tuple.b() instanceof TypeDescriptor)
                                         .map(tuple -> TypeDescriptor.class.cast(tuple.b()))
                                         .collect(toList());

        assertThat(methods, containsInAnyOrder(methodDescriptor(IgnoredTest.class, "ignoredTest"),
                                               methodDescriptor(DisabledTestWithoutMessage.class, "iHaveNoMessage")));
        assertThat(types, containsInAnyOrder(typeDescriptor(IgnoredTest.class),
                                             typeDescriptor(DisabledTestWithoutMessage.class)));

        store.commitTransaction();
    }

    /**
     * Verifies the group "junit:default".
     */
    @Test
    public void defaultGroup() throws RuleException {
        executeGroup("junit:Default");
        Map<String, Result<Constraint>> constraintViolations = reportPlugin.getConstraintResults();
        assertThat(constraintViolations, aMapWithSize(3));
        assertThat(constraintViolations.keySet(), hasItems("junit:IgnoreWithoutMessage",
                                                           "junit:AssertionMustProvideMessage",
                                                           "junit:TestMethodWithoutAssertion"));
    }

    /**
     * Verifies the constraint "junit:AssertionMustProvideMessage".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void assertionMustProvideMessage() throws Exception {
        scanClasses(Assertions4Junit4.class);
        assertThat(validateConstraint("junit:AssertionMustProvideMessage").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportPlugin.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit:AssertionMustProvideMessage")));
        List<Map<String, Object>> rows = result.getRows();
        assertThat(rows.size(), equalTo(1));
        assertThat((MethodDescriptor) rows.get(0).get("Method"), methodDescriptor(Assertions4Junit4.class, "assertWithoutMessage"));
        store.commitTransaction();
    }

    /**
     * Verifies the constraint "junit:TestMethodWithoutAssertion".
     *
     * @throws IOException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testMethodWithoutAssertion() throws Exception {
        scanClasses(Assertions4Junit4.class, Assertions4Junit5.class);
        assertThat(validateConstraint("junit:TestMethodWithoutAssertion").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportPlugin.getConstraintResults().values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit:TestMethodWithoutAssertion")));
        List<MethodDescriptor> methods = result.getRows().stream()
                                               .map(row -> row.get("Method"))
                                               .map(MethodDescriptor.class::cast).collect(toList());
        assertThat(methods.size(), equalTo(4));

        assertThat(methods, containsInAnyOrder(methodDescriptor(Assertions4Junit4.class, "testWithoutAssertion"),
                                               methodDescriptor(Assertions4Junit5.class, "repeatedTestWithoutAssertion"),
                                               methodDescriptor(Assertions4Junit5.class, "parameterizedTestWithoutAssertion", String.class),
                                               methodDescriptor(Assertions4Junit5.class, "testWithoutAssertion")));

        store.commitTransaction();
    }

    private class Tuple<A, B> {
        private A a;
        private B b;

        public Tuple(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A a() {
            return a;
        }

        public void a(A a) {
            this.a = a;
        }

        public B b() {
            return b;
        }

        public void b(B b) {
            this.b = b;
        }
    }
}
