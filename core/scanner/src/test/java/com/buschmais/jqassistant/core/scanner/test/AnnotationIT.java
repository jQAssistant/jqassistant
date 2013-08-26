package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.scanner.test.set.annotation.AnnotatedType;
import com.buschmais.jqassistant.core.scanner.test.set.annotation.Annotation;
import com.buschmais.jqassistant.core.scanner.test.set.annotation.NestedAnnotation;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.ValueDescriptorMatcher.annotationValueDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.ValueDescriptorMatcher.valueDescriptor;
import static com.buschmais.jqassistant.core.scanner.test.set.annotation.Enumeration.NON_DEFAULT;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Contains test which verify correct scanning of annotations and annotated types.
 */
public class AnnotationIT extends AbstractScannerIT {

    /**
     * Verifies an annotation on class level.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void annotatedClass() throws IOException, NoSuchFieldException {
        scanClasses(AnnotatedType.class, Annotation.class, NestedAnnotation.class);
        // verify annotation type
        TestResult testResult = executeQuery("MATCH (t:TYPE:CLASS)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:OF_TYPE]->(at:TYPE:ANNOTATION) RETURN t, a, at");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((TypeDescriptor) row.get("t"), typeDescriptor(AnnotatedType.class));
        assertThat((AnnotationValueDescriptor) row.get("a"), annotationValueDescriptor(Annotation.class, CoreMatchers.anything()));
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(Annotation.class));
        // verify values
        testResult = executeQuery("MATCH (t:TYPE:CLASS)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:HAS]->(value:VALUE) RETURN value");
        assertThat(testResult.getRows().size(), equalTo(5));
        List<Object> values = testResult.getColumns().get("value");
        assertThat(values, hasItem(valueDescriptor("value", is("class"))));
        assertThat(values, hasItem(valueDescriptor("classValue", typeDescriptor(Number.class))));
        assertThat(values, hasItem(valueDescriptor("arrayValue", allOf(hasItem(valueDescriptor("[0]", is("a"))), hasItem(valueDescriptor("[1]", is("b")))))));
        assertThat(values, hasItem(valueDescriptor("enumerationValue", fieldDescriptor(NON_DEFAULT))));
        assertThat(values, hasItem(valueDescriptor("nestedAnnotationValue", hasItem(valueDescriptor("value", is("nestedClass"))))));
    }

    /**
     * Verifies an annotation on method level.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void annotatedMethod() throws IOException, NoSuchFieldException, NoSuchMethodException {
        scanClasses(AnnotatedType.class, Annotation.class, NestedAnnotation.class);
        // verify annotation type on method level
        TestResult testResult = executeQuery("MATCH (m:METHOD)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:OF_TYPE]->(at:TYPE:ANNOTATION) RETURN m, a, at");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((MethodDescriptor) row.get("m"), methodDescriptor(AnnotatedType.class, "annotatedMethod", String.class));
        assertThat((AnnotationValueDescriptor) row.get("a"), annotationValueDescriptor(Annotation.class, anything()));
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(Annotation.class));
        // verify values on method level
        testResult = executeQuery("MATCH (m:METHOD)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:HAS]->(value:VALUE) RETURN value");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<Object> values = testResult.getColumns().get("value");
        assertThat(values, hasItem(valueDescriptor("value", is("method"))));
    }

    /**
     * Verifies an annotation on method parameter level.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void annotatedMethodParameter() throws IOException, NoSuchFieldException, NoSuchMethodException {
        scanClasses(AnnotatedType.class, Annotation.class, NestedAnnotation.class);
        // verify annotation type on method parameter level
        TestResult testResult = executeQuery("MATCH (m:METHOD)-[:HAS]->(p:PARAMETER)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:OF_TYPE]->(at:TYPE:ANNOTATION) RETURN m, a, at");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((MethodDescriptor) row.get("m"), methodDescriptor(AnnotatedType.class, "annotatedMethod", String.class));
        assertThat((AnnotationValueDescriptor) row.get("a"), annotationValueDescriptor(Annotation.class, anything()));
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(Annotation.class));
        // verify values on method parameter level
        testResult = executeQuery("MATCH (m:METHOD)-[:HAS]->(p:PARAMETER)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:HAS]->(value:VALUE) RETURN value");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<Object> values = testResult.getColumns().get("value");
        assertThat(values, hasItem(valueDescriptor("value", is("parameter"))));
    }

    /**
     * Verifies an annotation on field level.
     *
     * @throws IOException If the test fails.
     */
    @Test
    public void annotatedField() throws IOException, NoSuchFieldException, NoSuchMethodException {
        scanClasses(AnnotatedType.class, Annotation.class, NestedAnnotation.class);
        // verify annotation type
        TestResult testResult = executeQuery("MATCH (f:FIELD)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:OF_TYPE]->(at:TYPE:ANNOTATION) RETURN f, a, at");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((FieldDescriptor) row.get("f"), fieldDescriptor(AnnotatedType.class, "annotatedField"));
        assertThat((AnnotationValueDescriptor) row.get("a"), annotationValueDescriptor(Annotation.class, anything()));
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(Annotation.class));
        // verify values
        testResult = executeQuery("MATCH (f:FIELD)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:HAS]->(value:VALUE) RETURN value");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<Object> values = testResult.getColumns().get("value");
        assertThat(values, hasItem(valueDescriptor("value", is("field"))));
    }
}
