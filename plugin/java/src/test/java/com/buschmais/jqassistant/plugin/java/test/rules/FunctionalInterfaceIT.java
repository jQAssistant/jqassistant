package com.buschmais.jqassistant.plugin.java.test.rules;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.FunctionalInterface;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.FunctionalInterfaceWithoutAnnotation;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.MarkerInterface;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.NonFunctionalInterface;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Tests for the concept java:FunctionalInterface.
 */
public class FunctionalInterfaceIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:FunctionalInterface".
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void functionalInterface() throws Exception {
        scanClasses(FunctionalInterface.class, MarkerInterface.class, NonFunctionalInterface.class, FunctionalInterfaceWithoutAnnotation.class);
        assertThat(applyConcept("java:FunctionalInterface").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        TestResult result = query("MATCH (fi:Type:FunctionalInterface) RETURN fi");
        assertThat(result.getColumn("fi"),
                containsInAnyOrder(typeDescriptor(FunctionalInterface.class), typeDescriptor(FunctionalInterfaceWithoutAnnotation.class)));
        store.commitTransaction();
    }
}
