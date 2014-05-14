package com.buschmais.jqassistant.plugin.java8.test.rules;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java8.test.set.rules.FunctionalInterface;

/**
 * Tests for the concept java8:FunctionalInterface.
 */
public class FunctionalInterfaceIT extends AbstractPluginIT {

    /**
     * Verifies the concept "java8:FunctionalInterface".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void functionalInterface() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java8.test.set.rules.FunctionalInterface.class);
        applyConcept("java8:FunctionalInterface");
        store.beginTransaction();
        TestResult result = query("MATCH (fi:Type:FunctionalInterface) RETURN fi");
        assertThat(result.getColumn("fi"), hasItem(typeDescriptor(FunctionalInterface.class)));
        store.commitTransaction();
    }
}
