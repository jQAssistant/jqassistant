package com.buschmais.jqassistant.rules.java.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.buschmais.jqassistant.rules.java.test.set.java.ClassType;
import com.buschmais.jqassistant.rules.java.test.set.java.InterfaceType;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.buschmais.jqassistant.scanner.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the concept java:AssignableFrom.
 */
public class AssignableFromIT extends AbstractAnalysisIT {

    /**
     * Verifies the concept "java:AssignableFrom".
     * @throws IOException
     * @throws AnalyzerException
     */
    @Test
    public void assignableFrom() throws IOException, AnalyzerException {
        scanClasses(ClassType.class);
        applyConcept("java:AssignableFrom");
        Map<String, List<Object>> columns = executeQuery("MATCH (type:TYPE)<-[:ASSIGNABLE_FROM]-(assignableType) RETURN assignableType").getColumns();
        Matcher<Iterable<Object>> matcher = allOf(hasItem(typeDescriptor(ClassType.class)), hasItem(typeDescriptor(InterfaceType.class)), hasItem(typeDescriptor(Object.class)));
        assertThat(columns.get("assignableType"), matcher);
    }
}
