package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.AnnotationValueDescriptorMatcher.annotationValueDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.ValueDescriptorMatcher.valueDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Enumeration.NON_DEFAULT;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

/**
 * Contains test which verify correct scanning of annotations and annotated
 * types.
 */
public class AnnotationIT extends AbstractPluginIT {

	/**
	 * Verifies an annotation on class level.
	 * 
	 * @throws IOException
	 *             If the test fails.
	 */
	@Test
	public void annotatedClass() throws IOException, NoSuchFieldException {
		scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotatedType.class,
				com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class,
				com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.NestedAnnotation.class);
		// verify annotation type
		TestResult testResult = query("MATCH (t:TYPE:CLASS)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:OF_TYPE]->(at:TYPE:ANNOTATION) RETURN t, a, at");
		assertThat(testResult.getRows().size(), equalTo(1));
		Map<String, Object> row = testResult.getRows().get(0);
		assertThat((TypeDescriptor) row.get("t"),
				typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotatedType.class));
		assertThat(
				(AnnotationValueDescriptor) row.get("a"),
				annotationValueDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class,
						CoreMatchers.anything()));
		assertThat((TypeDescriptor) row.get("at"),
				typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class));
		// verify values
		testResult = query("MATCH (t:TYPE:CLASS)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:HAS]->(value:VALUE) RETURN value");
		assertThat(testResult.getRows().size(), equalTo(5));
		List<Object> values = testResult.getColumn("value");
		assertThat(values, hasItem(valueDescriptor("value", is("class"))));
		assertThat(values, hasItem(valueDescriptor("classValue", typeDescriptor(Number.class))));
		assertThat(
				values,
				hasItem(valueDescriptor("arrayValue",
						allOf(hasItem(valueDescriptor("[0]", is("a"))), hasItem(valueDescriptor("[1]", is("b")))))));
		assertThat(values, hasItem(valueDescriptor("enumerationValue", fieldDescriptor(NON_DEFAULT))));
		assertThat(values, hasItem(valueDescriptor("nestedAnnotationValue", hasItem(valueDescriptor("value", is("nestedClass"))))));
	}

	/**
	 * Verifies an annotation on method level.
	 * 
	 * @throws IOException
	 *             If the test fails.
	 */
	@Test
	public void annotatedMethod() throws IOException, NoSuchFieldException, NoSuchMethodException {
		scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotatedType.class,
				com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class,
				com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.NestedAnnotation.class);
		// verify annotation type on method level
		TestResult testResult = query("MATCH (m:METHOD)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:OF_TYPE]->(at:TYPE:ANNOTATION) RETURN m, a, at");
		assertThat(testResult.getRows().size(), equalTo(1));
		Map<String, Object> row = testResult.getRows().get(0);
		assertThat(
				(MethodDescriptor) row.get("m"),
				methodDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotatedType.class, "annotatedMethod",
						String.class));
		assertThat((AnnotationValueDescriptor) row.get("a"),
				annotationValueDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class, anything()));
		assertThat((TypeDescriptor) row.get("at"),
				typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class));
		// verify values on method level
		testResult = query("MATCH (m:METHOD)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:HAS]->(value:VALUE) RETURN value");
		assertThat(testResult.getRows().size(), equalTo(1));
		List<Object> values = testResult.getColumn("value");
		assertThat(values, hasItem(valueDescriptor("value", is("method"))));
	}

	/**
	 * Verifies an annotation on method parameter level.
	 * 
	 * @throws IOException
	 *             If the test fails.
	 */
	@Test
	public void annotatedMethodParameter() throws IOException, NoSuchFieldException, NoSuchMethodException {
		scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotatedType.class,
				com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class,
				com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.NestedAnnotation.class);
		// verify annotation type on method parameter level
		TestResult testResult = query("MATCH (m:METHOD)-[:HAS]->(p:PARAMETER)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:OF_TYPE]->(at:TYPE:ANNOTATION) RETURN m, a, at");
		assertThat(testResult.getRows().size(), equalTo(1));
		Map<String, Object> row = testResult.getRows().get(0);
		assertThat(
				(MethodDescriptor) row.get("m"),
				methodDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotatedType.class, "annotatedMethod",
						String.class));
		assertThat((AnnotationValueDescriptor) row.get("a"),
				annotationValueDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class, anything()));
		assertThat((TypeDescriptor) row.get("at"),
				typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class));
		// verify values on method parameter level
		testResult = query("MATCH (m:METHOD)-[:HAS]->(p:PARAMETER)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:HAS]->(value:VALUE) RETURN value");
		assertThat(testResult.getRows().size(), equalTo(1));
		List<Object> values = testResult.getColumn("value");
		assertThat(values, hasItem(valueDescriptor("value", is("parameter"))));
	}

	/**
	 * Verifies an annotation on field level.
	 * 
	 * @throws IOException
	 *             If the test fails.
	 */
	@Test
	public void annotatedField() throws IOException, NoSuchFieldException, NoSuchMethodException {
		scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotatedType.class,
				com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class,
				com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.NestedAnnotation.class);
		// verify annotation type
		TestResult testResult = query("MATCH (f:FIELD)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:OF_TYPE]->(at:TYPE:ANNOTATION) RETURN f, a, at");
		assertThat(testResult.getRows().size(), equalTo(1));
		Map<String, Object> row = testResult.getRows().get(0);
		assertThat((FieldDescriptor) row.get("f"),
				fieldDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotatedType.class, "annotatedField"));
		assertThat((AnnotationValueDescriptor) row.get("a"),
				annotationValueDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class, anything()));
		assertThat((TypeDescriptor) row.get("at"),
				typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Annotation.class));
		// verify values
		testResult = query("MATCH (f:FIELD)-[:ANNOTATED_BY]->(a:VALUE:ANNOTATION)-[:HAS]->(value:VALUE) RETURN value");
		assertThat(testResult.getRows().size(), equalTo(1));
		List<Object> values = testResult.getColumn("value");
		assertThat(values, hasItem(valueDescriptor("value", is("field"))));
	}

	/**
	 * Verifies dependencies generated by default values of annotation methods.
	 * 
	 * @throws IOException
	 *             If the test fails.
	 */
	@Test
	public void annotationDefaultValues() throws IOException, NoSuchFieldException, NoSuchMethodException {
		scanClasses(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotationWithDefaultValue.class);
		assertThat(query("MATCH (t:TYPE:ANNOTATION) RETURN t").getColumn("t"),
				hasItem(typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.AnnotationWithDefaultValue.class)));
		assertThat(
				query("MATCH (t:TYPE:ANNOTATION)-[:CONTAINS]->(m:METHOD)-[:DEPENDS_ON]->(d:TYPE) RETURN d").getColumn("d"),
				allOf(hasItem(typeDescriptor(Number.class)),
						hasItem(typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.Enumeration.class)),
						hasItem(typeDescriptor(double.class)), hasItem(typeDescriptor(Integer.class)),
						hasItem(typeDescriptor(com.buschmais.jqassistant.plugin.java.test.set.scanner.annotation.NestedAnnotation.class))));
	}
}
