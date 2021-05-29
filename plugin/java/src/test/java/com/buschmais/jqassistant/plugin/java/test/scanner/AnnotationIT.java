package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.*;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.AnnotationValueDescriptorMatcher.annotationValueDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.constructorDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.ValueDescriptorMatcher.valueDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Enumeration.DEFAULT;
import static com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Enumeration.NON_DEFAULT;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

/**
 * Contains test which verify correct scanning of annotations and annotated
 * types.
 */
public class AnnotationIT extends AbstractJavaPluginIT {

    /**
     * Verifies an annotation on class level.
     *
     */
    @Test
    public void annotatedClass() throws NoSuchFieldException {
        scanClasses(AnnotatedType.class, Annotation.class, NestedAnnotation.class, Enumeration.class);
        // verify annotation type
        store.beginTransaction();
        TestResult testResult = query(
                "MATCH (t:Type:Class)-[:ANNOTATED_BY]->(a:Java:ByteCode:Value:Annotation)-[:OF_TYPE]->(at:Type:Annotation) RETURN t, a, at");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((TypeDescriptor) row.get("t"), typeDescriptor(AnnotatedType.class));
        assertThat((AnnotationValueDescriptor) row.get("a"), annotationValueDescriptor(Annotation.class, anything()));
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(Annotation.class));
        // verify values
        testResult = query("MATCH (t:Type:Class)-[:ANNOTATED_BY]->(a:Java:ByteCode:Value:Annotation)-[:HAS]->(value:Value) RETURN value");
        assertThat(testResult.getRows().size(), equalTo(6));
        List<Object> values = testResult.getColumn("value");
        assertThat(values, hasItem(valueDescriptor("value", is("class"))));
        assertThat(values, hasItem(valueDescriptor("classValue", typeDescriptor(Number.class))));
        assertThat(values, hasItem(valueDescriptor("arrayValue", hasItems(valueDescriptor("[0]", is("a")), valueDescriptor("[1]", is("b"))))));
        assertThat(values, hasItem(valueDescriptor("enumerationValue", fieldDescriptor(NON_DEFAULT))));
        assertThat(values, hasItem(valueDescriptor("nestedAnnotationValue", hasItem(valueDescriptor("value", is("nestedClass"))))));
        assertThat(values,
                hasItem(valueDescriptor("nestedAnnotationValues", hasItem(valueDescriptor("[0]", hasItem(valueDescriptor("value", is("nestedClasses"))))))));
        store.commitTransaction();
    }

    /**
     * Verifies an annotation on method level.
     *
     */
    @Test
    public void annotatedMethod() throws ReflectiveOperationException {
        scanClasses(AnnotatedType.class, Annotation.class, NestedAnnotation.class);
        // verify annotation type on method level
        store.beginTransaction();
        TestResult testResult = query("MATCH (m:Method)-[:ANNOTATED_BY]->(a:Java:ByteCode:Value:Annotation)-[:OF_TYPE]->(at:Type:Annotation) RETURN m, a, at");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((MethodDescriptor) row.get("m"), methodDescriptor(AnnotatedType.class, "annotatedMethod", String.class));
        assertThat((AnnotationValueDescriptor) row.get("a"), annotationValueDescriptor(Annotation.class, anything()));
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(Annotation.class));
        // verify values on method level
        testResult = query("MATCH (m:Method)-[:ANNOTATED_BY]->(a:Java:ByteCode:Value:Annotation)-[:HAS]->(value:Value) RETURN value");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<Object> values = testResult.getColumn("value");
        assertThat(values, hasItem(valueDescriptor("value", is("method"))));
        store.commitTransaction();
    }

    /**
     * Verifies an annotation on method parameter level.
     *
     */
    @Test
    public void annotatedMethodParameter() throws ReflectiveOperationException {
        scanClasses(AnnotatedType.class, Annotation.class, NestedAnnotation.class);
        // verify annotation type on method parameter level
        store.beginTransaction();
        TestResult testResult = query(
                "MATCH (m:Method)-[:HAS]->(p:Parameter)-[:ANNOTATED_BY]->(a:Java:ByteCode:Value:Annotation)-[:OF_TYPE]->(at:Type:Annotation) RETURN m, a, at");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((MethodDescriptor) row.get("m"), methodDescriptor(AnnotatedType.class, "annotatedMethod", String.class));
        assertThat((AnnotationValueDescriptor) row.get("a"), annotationValueDescriptor(Annotation.class, anything()));
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(Annotation.class));
        // verify values on method parameter level
        testResult = query("MATCH (m:Method)-[:HAS]->(p:Parameter)-[:ANNOTATED_BY]->(a:Java:ByteCode:Value:Annotation)-[:HAS]->(value:Value) RETURN value");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<Object> values = testResult.getColumn("value");
        assertThat(values, hasItem(valueDescriptor("value", is("parameter"))));
        store.commitTransaction();
    }

    /**
     * Verifies an annotation on field level.
     *
     */
    @Test
    public void annotatedField() throws NoSuchFieldException {
        scanClasses(AnnotatedType.class, Annotation.class, NestedAnnotation.class);
        // verify annotation type
        store.beginTransaction();
        TestResult testResult = query("MATCH (f:Field)-[:ANNOTATED_BY]->(a:Java:ByteCode:Value:Annotation)-[:OF_TYPE]->(at:Type:Annotation) RETURN f, a, at");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((FieldDescriptor) row.get("f"), fieldDescriptor(AnnotatedType.class, "annotatedField"));
        assertThat((AnnotationValueDescriptor) row.get("a"), annotationValueDescriptor(Annotation.class, anything()));
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(Annotation.class));
        // verify values
        testResult = query("MATCH (f:Field)-[:ANNOTATED_BY]->(a:Java:ByteCode:Value:Annotation)-[:HAS]->(value:Value) RETURN value");
        assertThat(testResult.getRows().size(), equalTo(1));
        List<Object> values = testResult.getColumn("value");
        assertThat(values, hasItem(valueDescriptor("value", is("field"))));
        store.commitTransaction();
    }

    /**
     * Verifies dependencies generated by default values of annotation methods.
     *
     */
    @Test
    public void annotationDefaultValues() throws NoSuchFieldException {
        scanClasses(AnnotationWithDefaultValue.class);
        store.beginTransaction();
        assertThat(query("MATCH (t:Type:Annotation) RETURN t").getColumn("t"), hasItem(typeDescriptor(AnnotationWithDefaultValue.class)));
        assertThat(query("MATCH (t:Type:Annotation)-[:DECLARES]->(m:Method)-[:HAS_DEFAULT]->(v:Value) WHERE m.name='classValue' RETURN v").getColumn("v"),
                hasItem(valueDescriptor(null, typeDescriptor(Number.class))));
        assertThat(query("MATCH (t:Type:Annotation)-[:DECLARES]->(m:Method)-[:HAS_DEFAULT]->(v:Value) WHERE m.name='enumerationValue' RETURN v").getColumn("v"),
                hasItem(valueDescriptor(null, fieldDescriptor(DEFAULT))));
        assertThat(query("MATCH (t:Type:Annotation)-[:DECLARES]->(m:Method)-[:HAS_DEFAULT]->(v:Value) WHERE m.name='primitiveValue' RETURN v").getColumn("v"),
                hasItem(valueDescriptor(null, is(0d))));
        assertThat(query("MATCH (t:Type:Annotation)-[:DECLARES]->(m:Method)-[:HAS_DEFAULT]->(v:Value) WHERE m.name='arrayValue' RETURN v").getColumn("v"),
                hasItem(valueDescriptor(null, hasItem(valueDescriptor("[0]", typeDescriptor(Integer.class))))));
        assertThat(query("MATCH (t:Type:Annotation)-[:DECLARES]->(m:Method)-[:HAS_DEFAULT]->(v:Value) WHERE m.name='annotationValue' RETURN v").getColumn("v"),
                hasItem(annotationValueDescriptor(NestedAnnotation.class, hasItem(valueDescriptor("value", is("test"))))));
        store.commitTransaction();
    }

    /**
     * Verifies dependencies generated by default values of annotation methods.
     *
     */
    @Test
    public void innerClass() throws NoSuchMethodException {
        scanClasses(AnnotatedType.GenericInnerAnnotatedType.class, Annotation.class);
        store.beginTransaction();
        TestResult testResult = query("MATCH (c:Constructor)-[:HAS]->(:Parameter)-[:ANNOTATED_BY]->()-[:OF_TYPE]->(Type:Annotation) RETURN c");
        assertThat(testResult.getRows().size(), equalTo(1));
        assertThat(testResult.getColumn("c"), hasItem(constructorDescriptor(AnnotatedType.GenericInnerAnnotatedType.class, AnnotatedType.class, Object.class)));
        store.commitTransaction();
    }
}
