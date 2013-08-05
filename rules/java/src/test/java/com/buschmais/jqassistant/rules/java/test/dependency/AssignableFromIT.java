package com.buschmais.jqassistant.rules.java.test.dependency;

import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.buschmais.jqassistant.scanner.test.set.pojo.Pojo;
import com.buschmais.jqassistant.store.api.QueryResult;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.scanner.test.matcher.ClassDescriptorMatcher.classDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the concept java:AssignableFrom.
 */
public class AssignableFromIT extends AbstractAnalysisIT {

    @Test
    public void assignableFrom() throws IOException, ReportWriterException {
        scanClasses(Pojo.class);
        applyConcept("java:AssignableFrom");
        TestResult testResult = executeQuery("MATCH (pojo:CLASS)<-[:ASSIGNABLE_FROM]-(c) RETURN c");
        Map<String, List<Object>> columns = testResult.getColumns();
        Matcher<Iterable<Object>> matcher = allOf(hasItem(classDescriptor(Object.class)), hasItem(classDescriptor(Pojo.class)));
        assertThat(columns.get("c"), matcher);
    }
}
