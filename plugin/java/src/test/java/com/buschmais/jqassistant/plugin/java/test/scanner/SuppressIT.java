package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.SuppressionDescriptor;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.api.model.JavaSuppressDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress.DeprecatedSuppress;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress.Suppress;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class SuppressIT extends AbstractJavaPluginIT {

    @ParameterizedTest
    @ValueSource(classes = { Suppress.class, DeprecatedSuppress.class })
    void suppressAnnotationWithUntilAndReasonAttributes(Class<?> classToScan) {
        scanClasses(classToScan);
        List<Map<String, Object>> rows = query("MATCH (suppress:Java:jQASuppress) RETURN suppress").getRows();
        assertThat(rows.size()).isEqualTo(3);
        store.beginTransaction();
        verifySuppressions(rows, 0, 1, "For testing this annotation", null);
        verifySuppressions(rows, 1, 1, null, null);
        verifySuppressions(rows, 2, 3, "Reason for suppression", LocalDate.parse("2075-08-13"));
        store.commitTransaction();
    }

    private static void verifySuppressions(List<Map<String, Object>> rows, int index, int expectedSuppressions, String expectedReason,
        LocalDate expectedUntil) {
        List<SuppressionDescriptor> suppressions = ((JavaSuppressDescriptor) rows.get(index)
            .get("suppress")).getSuppressions();
        assertThat(suppressions.size()).isEqualTo(expectedSuppressions);
        for (SuppressionDescriptor suppression : suppressions) {
            assertThat(suppression.getReason()).isEqualTo(expectedReason);
            assertThat(suppression.getUntil()).isEqualTo(expectedUntil);
        }
    }

    @ParameterizedTest
    @ValueSource(classes = { Suppress.class, DeprecatedSuppress.class })
    void suppressAnnotationMustNotBeScanned(Class<?> classToScan) throws RuleException {
        scanClasses(classToScan);
        Result<Constraint> constraintResult = validateConstraint("test-suppress:SuppressAnnotationMustNotBeScanned");
        assertThat(constraintResult.getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        assertThat(constraintResult.getRows()
            .size()).isEqualTo(0);
        store.commitTransaction();
    }

    @ParameterizedTest
    @ValueSource(classes = { Suppress.class, DeprecatedSuppress.class })
    void suppressedClass(Class<?> classToScan) throws RuleException {
        verifySuppress(classToScan, "test-suppress:Class", "test-suppress:SuppressedClass", "class");
    }

    @ParameterizedTest
    @ValueSource(classes = { Suppress.class, DeprecatedSuppress.class })
    void suppressedField(Class<?> classToScan) throws RuleException {
        verifySuppress(classToScan, "test-suppress:Field", "test-suppress:SuppressedField", "field");
    }

    @ParameterizedTest
    @ValueSource(classes = { Suppress.class, DeprecatedSuppress.class })
    void suppressedMethod(Class<?> classToScan) throws RuleException {
        verifySuppress(classToScan, "test-suppress:Method", "test-suppress:SuppressedMethod", "method");
    }

    @ParameterizedTest
    @ValueSource(classes = { Suppress.class, DeprecatedSuppress.class })
    void suppressedMethodInPrimaryColumn(Class<?> classToScan) throws RuleException {
        verifySuppress(classToScan, "test-suppress:MethodInPrimaryColumn", "test-suppress:SuppressedMethodInPrimaryColumn", "method");
    }

    @ParameterizedTest
    @ValueSource(classes = { Suppress.class, DeprecatedSuppress.class })
    void suppressedMethodInNonPrimaryColumn(Class<?> classToScan) throws RuleException {
        verifySuppress(classToScan, "test-suppress:MethodInNonPrimaryColumn", "test-suppress:SuppressedMethodInNonPrimaryColumn", "method");
    }

    private void verifySuppress(Class<?> classToScan, String constraintId, String conceptId, String column) throws RuleException {
        scanClasses(classToScan);
        assertThat(validateConstraint(constraintId).getStatus()).isEqualTo(SUCCESS);
        Result<Concept> suppressedItems = applyConcept(conceptId);
        assertThat(suppressedItems.getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        assertThat(suppressedItems.getRows()
            .size()).isEqualTo(1);
        Row row = suppressedItems.getRows()
            .get(0);
        JavaSuppressDescriptor suppressDescriptor = (JavaSuppressDescriptor) row.getColumns()
            .get(column)
            .getValue();
        List<String> constraintIds = suppressDescriptor.getSuppressions()
            .stream()
            .map(SuppressionDescriptor::getRuleId)
            .collect(toList());
        assertThat(constraintIds).contains(constraintId);
        store.commitTransaction();
    }
}
