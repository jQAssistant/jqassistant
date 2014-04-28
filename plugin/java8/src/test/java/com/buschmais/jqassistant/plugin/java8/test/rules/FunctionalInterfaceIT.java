package com.buschmais.jqassistant.plugin.java8.test.rules;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java8.test.set.rules.*;
import com.buschmais.jqassistant.plugin.java8.test.set.rules.FunctionalInterface;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InterfaceType;

/**
 * Tests for the concept java8:FunctionalInterface.
 */
public class FunctionalInterfaceIT extends AbstractPluginIT {

    /**
     * Verifies the concept "java8:FunctionalInterface".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalyzerException
     *             If the test fails.
     */
    @Test
    public void functionalInterface() throws IOException, AnalyzerException, NoSuchMethodException {
        scanClasses(com.buschmais.jqassistant.plugin.java8.test.set.rules.FunctionalInterface.class);
        applyConcept("java8:FunctionalInterface");
        store.beginTransaction();
        TestResult result = query("MATCH (fi:TYPE:FunctionalInterface) RETURN fi");
        assertThat(result.getColumn("fi"), hasItem(typeDescriptor(FunctionalInterface.class)));
        store.commitTransaction();
    }
}
