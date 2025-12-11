package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
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
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

class SuppressIT extends AbstractJavaPluginIT {

    @ParameterizedTest
    @ValueSource(classes = { Suppress.class, DeprecatedSuppress.class })
    void suppressAnnotationWithUntilAndReasonAttributes(Class<?> classToScan) {
        scanClasses(classToScan);
        List<Map<String, Object>> rows = query("MATCH (type:Java:jQASuppress) return type").getRows();
        assertThat(rows.size()).isEqualTo(3);
        store.beginTransaction();
        assertThat(((JavaSuppressDescriptor) rows.get(0)
            .get("type")).getSuppressReason()).isEqualTo("For testing this annotation");
        assertThat(((JavaSuppressDescriptor) rows.get(0)
            .get("type")).getSuppressUntil()).isNull();
        assertThat(((JavaSuppressDescriptor) rows.get(1)
            .get("type")).getSuppressReason()).isNull();
        assertThat(((JavaSuppressDescriptor) rows.get(1)
            .get("type")).getSuppressUntil()).isNull();
        assertThat(((JavaSuppressDescriptor) rows.get(2)
            .get("type")).getSuppressReason()).isEqualTo("Reason for suppression");
        assertThat(((JavaSuppressDescriptor) rows.get(2)
            .get("type")).getSuppressUntil()).isEqualTo("2075-08-13");
        store.commitTransaction();
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
        Result<Concept> supressedItems = applyConcept(conceptId);
        assertThat(supressedItems.getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        assertThat(supressedItems.getRows()
            .size()).isEqualTo(1);
        Row row = supressedItems.getRows()
            .get(0);
        JavaSuppressDescriptor suppressDescriptor = (JavaSuppressDescriptor) row.getColumns()
            .get(column)
            .getValue();
        assertThat(asList(suppressDescriptor.getSuppressIds()), hasItem(constraintId));
        store.commitTransaction();
    }
}
