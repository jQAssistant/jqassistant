package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import com.buschmais.jqassistant.core.model.test.matcher.descriptor.ValueDescriptorMatcher;
import com.buschmais.jqassistant.core.scanner.test.set.annotation.AnnotatedType;
import com.buschmais.jqassistant.core.scanner.test.set.annotation.Annotation;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.ValueDescriptorMatcher.annotationValueDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Contains test which verify correct scanning of annotations and annotated types.
 */
public class AnnotationIT extends AbstractScannerIT {

    /**
     * @throws IOException
     */
    @Test
    public void annotatedClass() throws IOException, NoSuchFieldException {
        scanClasses(AnnotatedType.class, Annotation.class);
        TestResult testResult = executeQuery("MATCH (t:TYPE)-[:ANNOTATED_BY]->(a:ANNOTATION)-[:OF_TYPE]->(at:TYPE:ANNOTATION) RETURN t, a, at");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((TypeDescriptor) row.get("t"), typeDescriptor(AnnotatedType.class));
        assertThat((AnnotationValueDescriptor) row.get("a"), annotationValueDescriptor(Annotation.class, CoreMatchers.anything()));
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(Annotation.class));
    }
}
