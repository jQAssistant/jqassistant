package com.buschmais.jqassistant.rules.java.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.test.AbstractAnalysisIT;
import com.buschmais.jqassistant.rules.java.test.set.java.ClassType;
import com.buschmais.jqassistant.rules.java.test.set.java.InterfaceType;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.core.model.test.matcher.descriptor.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Tests for the concept java:AssignableFrom.
 */
public class AssignableFromIT extends AbstractAnalysisIT {

    /**
     * Verifies the concept "java:AssignableFrom".
     *
     * @throws IOException       If the test fails.
     * @throws AnalyzerException If the test fails.
     */
    @Test
    public void assignableFrom() throws IOException, AnalyzerException {
        scanClasses(ClassType.class);
        applyConcept("java:AssignableFrom");
        assertThat(query("MATCH (types:TYPE)<-[:ASSIGNABLE_FROM]-(assignableType) RETURN assignableType").getColumns().get("assignableType"), allOf(hasItem(typeDescriptor(ClassType.class)), hasItem(typeDescriptor(InterfaceType.class)), hasItem(typeDescriptor(Object.class))));
    }
}
