package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InterfaceType;

/**
 * Tests for the concept java:TypeAssignableFrom.
 */
public class TypeAssignableFromIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:AssignableFrom".
     * 
     * @throws IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void assignableFrom() throws IOException, AnalysisException {
        scanClasses(ClassType.class);
        assertThat(applyConcept("java:TypeAssignableFrom").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat(query("MATCH (types:Type)<-[:ASSIGNABLE_FROM]-(assignableType) RETURN assignableType").getColumn("assignableType"),
                allOf(hasItem(typeDescriptor(ClassType.class)), hasItem(typeDescriptor(InterfaceType.class)), hasItem(typeDescriptor(Object.class))));
        store.commitTransaction();
    }
}
