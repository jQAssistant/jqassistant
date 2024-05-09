package com.buschmais.jqassistant.plugin.java.test.rules;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.FunctionalInterface;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.FunctionalInterfaceWithoutAnnotation;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.MarkerInterface;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.NonFunctionalInterface;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the concept java:FunctionalInterface.
 */
class FunctionalInterfaceIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "java:FunctionalInterface".
     *
     * @throws java.io.IOException
     *     If the test fails.
     */
    @Test
    void functionalInterface() throws Exception {
        scanClasses(FunctionalInterface.class, MarkerInterface.class, NonFunctionalInterface.class, FunctionalInterfaceWithoutAnnotation.class);
        assertThat(applyConcept("java:FunctionalInterface").getStatus()).isEqualTo(SUCCESS);
        store.beginTransaction();
        TestResult result = query("MATCH (fi:Type:FunctionalInterface) RETURN fi");
        assertThat(result.<TypeDescriptor>getColumn("fi")).haveExactly(1, typeDescriptor(FunctionalInterface.class))
            .haveExactly(1, typeDescriptor(FunctionalInterfaceWithoutAnnotation.class));
        store.commitTransaction();
    }
}
