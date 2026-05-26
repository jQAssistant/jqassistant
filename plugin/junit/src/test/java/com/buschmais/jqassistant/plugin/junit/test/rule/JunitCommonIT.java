package com.buschmais.jqassistant.plugin.junit.test.rule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.ClassTypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition;
import com.buschmais.jqassistant.plugin.junit.api.scanner.JunitScope;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.Assertions4Junit4;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.IgnoredTest;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.IgnoredTestWithReason;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.report.AbstractExample;
import com.buschmais.jqassistant.plugin.junit.test.set.junit4.report.Example;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.AbstractAssertions4Junit5;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.Assertions4Junit5;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.DisabledTestWithReason;
import com.buschmais.jqassistant.plugin.junit.test.set.junit5.DisabledTestWithoutReason;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.core.test.matcher.ConstraintMatcher.constraint;
import static com.buschmais.jqassistant.core.test.matcher.ResultMatcher.result;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;

public class JunitCommonIT extends AbstractJunitIT {

    /**
     * Verifies the concept "junit:TestCaseImplementedByMethod".
     *
     * @throws IOException
     *     If the test fails.
     * @throws NoSuchMethodException
     *     If the test fails.
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
     *     If the test fails.
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    public void testCaseImplementedByMethodUnique() throws Exception {
        scanClasses(Example.class);
        scanClassPathResource(JunitScope.TESTREPORTS, "/TEST-com.buschmais.jqassistant.plugin.junit4.test.set.Example.xml");
        store.beginTransaction();
        // create existing relations with and without properties
        assertThat(
            query("MATCH (t:TestCase {name: 'success'}), (m:Method {name: 'success'}) MERGE (t)-[r:IMPLEMENTED_BY {prop: 'value'}]->(m) RETURN r").getColumn(
                    "r")
                .size(), equalTo(1));
        assertThat(query("MATCH (t:TestCase {name: 'failure'}), (m:Method {name: 'failure'}) MERGE (t)-[r:IMPLEMENTED_BY]->(m) RETURN r").getColumn("r")
            .size(), equalTo(1));
        assertThat(
            query("MATCH (t:TestCase {name: 'success'}), (c:Type {name: 'Example'}) MERGE (t)-[r:DEFINED_BY {prop: 'value'}]->(c) RETURN r").getColumn("r")
                .size(), equalTo(1));
        assertThat(query("MATCH (t:TestCase {name: 'failure'}), (c:Type {name: 'Example'}) MERGE (t)-[r:DEFINED_BY]->(c) RETURN r").getColumn("r")
            .size(), equalTo(1));
        verifyUniqueRelation("IMPLEMENTED_BY", 2);
        verifyUniqueRelation("DEFINED_BY", 2);
        store.commitTransaction();
        assertThat(applyConcept("junit:TestCaseImplementedByMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        verifyUniqueRelation("IMPLEMENTED_BY", 4);
        verifyUniqueRelation("DEFINED_BY", 5);
        store.commitTransaction();
    }

    @Test
    public void conceptDisabled() throws Exception {
        scanClasses(DisabledTestWithReason.class, DisabledTestWithoutReason.class);
        final Result<Concept> conceptResult = applyConcept("junit:Disabled");
        assertThat(conceptResult.getStatus(), equalTo(SUCCESS));
        assertThat(conceptResult.getRows()).hasSize(4);

        store.beginTransaction();
        assertDisabledElements(conceptResult.getRows().stream()
            .map(JunitCommonIT::unpackRow)
            .collect(toList()));

        final TestResult queryResult = query("MATCH (t:Type)-[:DECLARES*0..1]->(e:JUnit:Inactive:Test) RETURN t AS DeclaringType, e AS Element, e.inactiveReason AS Reason");
        assertThat(queryResult.getRows()).hasSize(4);
        assertDisabledElements(queryResult.getRows());

        store.commitTransaction();
    }

    @Test
    public void conceptIgnore() throws Exception {
        scanClasses(IgnoredTest.class, IgnoredTestWithReason.class);
        final Result<Concept> conceptResult = applyConcept("junit:Ignore");
        assertThat(conceptResult.getStatus(), equalTo(SUCCESS));
        assertThat(conceptResult.getRows()).hasSize(4);

        store.beginTransaction();
        assertIgnoredElements(conceptResult.getRows().stream()
            .map(JunitCommonIT::unpackRow)
            .collect(toList()));

        final TestResult queryResult = query("MATCH (t:Type)-[:DECLARES*0..1]->(e:JUnit:Inactive:Test) RETURN t AS DeclaringType, e AS Element, e.inactiveReason AS Reason");
        assertThat(queryResult.getRows()).hasSize(4);
        assertIgnoredElements(queryResult.getRows());

        store.commitTransaction();
    }

    @Test
    public void abstractConceptInactive() throws Exception {
        scanClasses(IgnoredTest.class, IgnoredTestWithReason.class, DisabledTestWithReason.class, DisabledTestWithoutReason.class);
        final Result<Concept> conceptResult = applyConcept("java:InactiveTest");
        assertThat(conceptResult.getStatus(), equalTo(SUCCESS));
        assertThat(conceptResult.getRows()).hasSize(8);

        store.beginTransaction();
        assertIgnoredElements(conceptResult.getRows().stream()
            .map(JunitCommonIT::unpackRow)
            .collect(toList()));
        assertDisabledElements(conceptResult.getRows().stream()
            .map(JunitCommonIT::unpackRow)
            .collect(toList()));

        store.commitTransaction();
    }

    private static void assertDisabledElements(List<Map<String, Object>> rows) throws NoSuchMethodException {
        assertInactiveElement(rows, typeDescriptor(DisabledTestWithoutReason.class), typeDescriptor(DisabledTestWithoutReason.class), TypeDescriptor.class, null);
        assertInactiveElement(rows, typeDescriptor(DisabledTestWithoutReason.class), methodDescriptor(DisabledTestWithoutReason.class, "iHaveNoMessage"), MethodDescriptor.class, null);
        assertInactiveElement(rows, typeDescriptor(DisabledTestWithReason.class), typeDescriptor(DisabledTestWithReason.class), TypeDescriptor.class, "message");
        assertInactiveElement(rows, typeDescriptor(DisabledTestWithReason.class), methodDescriptor(DisabledTestWithReason.class, "iHaveAMessage"), MethodDescriptor.class, "message");
    }

    private static void assertIgnoredElements(List<Map<String, Object>> rows) throws NoSuchMethodException {
        assertInactiveElement(rows, typeDescriptor(IgnoredTest.class), typeDescriptor(IgnoredTest.class), TypeDescriptor.class, null);
        assertInactiveElement(rows, typeDescriptor(IgnoredTest.class), methodDescriptor(IgnoredTest.class, "ignoredTest"), MethodDescriptor.class, null);
        assertInactiveElement(rows, typeDescriptor(IgnoredTestWithReason.class), typeDescriptor(IgnoredTestWithReason.class), TypeDescriptor.class, "ignored");
        assertInactiveElement(rows, typeDescriptor(IgnoredTestWithReason.class), methodDescriptor(IgnoredTestWithReason.class, "ignoredTestWithMessage"), MethodDescriptor.class, "ignored");
    }

    private static Map<String, Object> unpackRow(Row row) {
        final HashMap<String, Object> result = new HashMap<>();
        for(String key : row.getColumns().keySet()) {
            final Object value = row.getColumns().get(key) == null ? null : row.getColumns().get(key).getValue();
            result.put(key, value);
        }
        return result;
    }

    private static <T> void  assertInactiveElement(List<Map<String, Object>> rows, TypeDescriptorCondition declaringType, Condition<T> elementCondition, Class<T> elementType, String reason) {
        final List<Map<String, Object>> sublist = rows.stream()
            .filter(row -> elementType.isInstance(row.get("Element")))
            .filter(row -> elementCondition.matches(elementType.cast(row.get("Element"))))
            .collect(toList());

        assertThat(sublist).hasSize(1);
        assertThat(sublist.get(0).get("DeclaringType")).asInstanceOf(type(TypeDescriptor.class)).is(declaringType);
        assertThat(sublist.get(0).get("Reason")).isEqualTo(reason);
    }

    /**
     * Verifies the concept "junit:InactiveTestWithoutReason".
     *
     * @throws IOException
     *     If the test fails.
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    public void inactiveTestWithoutReason() throws Exception {
        scanClasses(IgnoredTest.class, IgnoredTestWithReason.class, DisabledTestWithReason.class, DisabledTestWithoutReason.class);
        assertThat(validateConstraint("junit:InactiveTestWithoutReason").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        List<Result<Constraint>> constraintViolations = new ArrayList<>(reportPlugin.getConstraintResults()
            .values());
        assertThat(constraintViolations.size(), equalTo(1));
        Result<Constraint> result = constraintViolations.get(0);
        assertThat(result, result(constraint("junit:InactiveTestWithoutReason")));
        List<Row> rows = result.getRows();
        List<Map.Entry<String, String>> typesAndIgnoredType = new ArrayList<>();

        for (Row row : rows) {
            String type = ((ClassTypeDescriptor) row.getColumns()
                .get("DeclaringClass")
                .getValue()).getName();

            String ignoredType;
            Object inactiveTestWithoutReason = row.getColumns()
                .get("InactiveTestWithoutReason")
                .getValue();
            if (inactiveTestWithoutReason instanceof ClassTypeDescriptor) {
                ignoredType = ((ClassTypeDescriptor) inactiveTestWithoutReason).getName();
            } else {
                ignoredType = ((MethodDescriptor) inactiveTestWithoutReason).getName();
            }
            typesAndIgnoredType.add(Map.entry(type, ignoredType));
        }
        assertThat(typesAndIgnoredType.size()).isEqualTo(4);
        assertThat(typesAndIgnoredType).contains(Map.entry("IgnoredTest", "IgnoredTest"));
        assertThat(typesAndIgnoredType).contains(Map.entry("IgnoredTest", "ignoredTest"));
        assertThat(typesAndIgnoredType).contains(Map.entry("DisabledTestWithoutReason", "DisabledTestWithoutReason"));
        assertThat(typesAndIgnoredType).contains(Map.entry("DisabledTestWithoutReason", "iHaveNoMessage"));

        store.commitTransaction();
    }

    /**
     * Verifies the group "junit:default".
     */
    @Test
    public void defaultGroup() throws RuleException {
        executeGroup("junit:Default");
        Map<String, Result<Constraint>> constraintViolations = reportPlugin.getConstraintResults();
        assertThat(constraintViolations, aMapWithSize(1));
        assertThat(constraintViolations.keySet(),
            hasItems("junit:InactiveTestWithoutReason"));
    }

    /**
     * Verifies the constraint "junit:TestMethodWithoutAssertion".
     *
     * @throws IOException
     *     If the test fails.
     * @throws NoSuchMethodException
     *     If the test fails.
     */
    @Test
    public void testMethodWithoutAssertion() throws Exception {

        /*
         Test classes belonging to our test set are ignored or disable for technical reasons. To validate this
         constraint, we have to remove the according label from the class nodes.
         */

        scanClasses(Assertions4Junit4.class, Assertions4Junit5.class, AbstractAssertions4Junit5.class);

        // Execute the required concept explicitly
        applyConcept("java:InactiveTest");

        // Remove the labels from class nodes
        final String query = "MATCH (t:Type:JUnit:Inactive:Test {fqn: \"%s\"}) REMOVE t:JUnit:Inactive:Test";
        query(String.format(query, Assertions4Junit4.class.getName()));
        query(String.format(query, Assertions4Junit5.class.getName()));
        query(String.format(query, AbstractAssertions4Junit5.class.getName()));

        // Execute the constraint
        assertThat(validateConstraint("java:TestMethodWithoutAssertion").getStatus(), equalTo(FAILURE));
        store.beginTransaction();
        Result<Constraint> result = reportPlugin.getConstraintResults()
            .get("java:TestMethodWithoutAssertion");
        assertThat(result, result(constraint("java:TestMethodWithoutAssertion")));
        List<MethodDescriptor> methods = result.getRows()
            .stream()
            .map(row -> row.getColumns()
                .get("TestMethod")
                .getValue())
            .map(MethodDescriptor.class::cast)
            .collect(toList());
        assertThat(methods).hasSize(5);
        assertThat(methods).haveExactly(1, methodDescriptor(Assertions4Junit4.class, "testWithoutAssertion"));
        assertThat(methods).haveExactly(1, methodDescriptor(Assertions4Junit5.class, "repeatedTestWithoutAssertion"));
        assertThat(methods).haveExactly(1, methodDescriptor(Assertions4Junit5.class, "parameterizedTestWithoutAssertion", String.class));
        assertThat(methods).haveExactly(1, methodDescriptor(Assertions4Junit5.class, "testWithDeepNestedAssertion"));
        assertThat(methods).haveExactly(1, methodDescriptor(Assertions4Junit5.class, "testWithoutAssertion"));
        store.commitTransaction();
    }

    @Test
    public void testMethodWithoutAssertionInaciveClasses() throws Exception {

        /*
         Test classes belonging to our test set are ignored or disable for technical reasons. Hence, there are no
         violations of this constraint.
         */

        scanClasses(Assertions4Junit4.class, Assertions4Junit5.class, AbstractAssertions4Junit5.class);
        final Result<Constraint> result = validateConstraint("java:TestMethodWithoutAssertion");
        assertThat(result.getStatus(), equalTo(SUCCESS));
        assertThat(result.getRows()).isEmpty();
    }
}
