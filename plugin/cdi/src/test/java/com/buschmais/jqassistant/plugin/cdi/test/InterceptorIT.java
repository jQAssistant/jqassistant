package com.buschmais.jqassistant.plugin.cdi.test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.interceptor.CustomBinding;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.interceptor.CustomInterceptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

/**
 * Tests for the interceptor concepts.
 */
public class InterceptorIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept "cdi:Interceptor".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void interceptor() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(CustomInterceptor.class);
        applyConcept("interceptor:Interceptor");
        store.beginTransaction();
        List<Object> column = query("MATCH (i:Interceptor) RETURN i").getColumn("i");
        assertThat(column, hasItem(typeDescriptor(CustomInterceptor.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept "interceptor:Binding".
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void interceptorBinding() throws IOException, AnalysisException, NoSuchMethodException, NoSuchFieldException {
        scanClasses(CustomBinding.class);
        applyConcept("interceptor:Binding");
        store.beginTransaction();
        List<Object> column = query("MATCH (b:Interceptor:Binding) RETURN b").getColumn("b");
        assertThat(column, hasItem(typeDescriptor(CustomBinding.class)));
        store.commitTransaction();
    }

}
