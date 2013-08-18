package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.test.set.annotation.AnnotatedType;
import com.buschmais.jqassistant.core.scanner.test.set.annotation.Annotation;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
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
    public void annotatedClass() throws IOException {
        scanClasses(AnnotatedType.class, Annotation.class);
        TestResult testResult = executeQuery("MATCH (at:TYPE)-[:ANNOTATED_BY]->(a:TYPE:ANNOTATION) RETURN at, a");
        assertThat(testResult.getRows().size(), equalTo(1));
        Map<String, Object> row = testResult.getRows().get(0);
        assertThat((TypeDescriptor) row.get("at"), typeDescriptor(AnnotatedType.class));
        assertThat((TypeDescriptor) row.get("a"), typeDescriptor(Annotation.class));
    }
}
