package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.test.matcher.ClassDescriptorMatcher;
import com.buschmais.jqassistant.scanner.test.sets.annotation.AnnotatedType;
import com.buschmais.jqassistant.scanner.test.sets.annotation.Annotation;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.QueryResult;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class AnnotationIT extends AbstractScannerIT {

    @Test
    public void classAnnotation() throws IOException {
        store.beginTransaction();
        scanner.scanClasses(AnnotatedType.class, Annotation.class);
        store.endTransaction();
        QueryResult result = store.executeQuery("MATCH (at:CLASS)-[:ANNOTATED_BY]->(a:CLASS) RETURN at.FQN, a.FQN");
        Iterable<Map<String, Object>> rows = result.getRows();
        Map<String, ?> row = rows.iterator().next();
        assertThat(row, hasEntry("a.FQN", (Object) Annotation.class.getName()));
        assertThat(row, hasEntry("at.FQN", (Object) AnnotatedType.class.getName()));
    }
}
