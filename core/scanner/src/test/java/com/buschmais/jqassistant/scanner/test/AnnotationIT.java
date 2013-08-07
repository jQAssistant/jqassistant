package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.scanner.test.set.annotation.AnnotatedType;
import com.buschmais.jqassistant.scanner.test.set.annotation.Annotation;
import com.buschmais.jqassistant.store.api.QueryResult;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.scanner.test.matcher.TypeDescriptorMatcher.classDescriptor;
import static org.junit.Assert.assertThat;

public class AnnotationIT extends AbstractScannerIT {

    @Test
    public void classAnnotation() throws IOException {
        scanClasses(AnnotatedType.class, Annotation.class);
        QueryResult result = store.executeQuery("MATCH (at:TYPE)-[:ANNOTATED_BY]->(a:TYPE) RETURN at, a");
        Iterable<QueryResult.Row> rows = result.getRows();
        QueryResult.Row row = rows.iterator().next();
        TypeDescriptor a = row.get("a");
        TypeDescriptor at = row.get("at");
        assertThat(at, classDescriptor(AnnotatedType.class));
        assertThat(a, classDescriptor(Annotation.class));
    }
}
