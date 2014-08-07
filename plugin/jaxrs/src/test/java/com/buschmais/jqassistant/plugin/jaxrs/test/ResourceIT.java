package com.buschmais.jqassistant.plugin.jaxrs.test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.jaxrs.test.set.beans.MyRestResource;

/**
 * Test to verify JAX-RS Resource concepts.
 * 
 * @author Aparna Chaudhary
 */
public class ResourceIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept {@code jaxrs:Resource}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void test_Resource_Concept() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(MyRestResource.class);
        applyConcept("jaxrs:Resource");
        store.beginTransaction();
        assertThat("Expected RestResource", query("MATCH (getResource:JaxRS:Resource) RETURN getResource").getColumn("getResource"),
                hasItem(typeDescriptor(MyRestResource.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept {@code jaxrs:Resource} is not applied to invalid
     * Resource classes.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void testInvalid_Resource_Concept() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(ResourceIT.class);
        applyConcept("jaxrs:Resource");
        store.beginTransaction();
        assertThat("Unexpected RestResource", query("MATCH (getResource:JaxRS:Resource) RETURN getResource").getColumn("getResource"), nullValue());
        store.commitTransaction();

    }

}
