package com.buschmais.jqassistant.plugin.jaxrs.test;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.jaxrs.test.set.beans.MyRestResource;

/**
 * Test to verify JAX-RS Resource method concepts.
 * 
 * @author Aparna Chaudhary
 */
public class ResourceMethodIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept {@code jaxrs:GetResourceMethod}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void GetResourceMethodConcept() throws Exception {
        scanClasses(MyRestResource.class);
        assertThat(applyConcept("jaxrs:GetResourceMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected GetResourceMethod", query("MATCH (resourceMethod:JaxRS:GetResourceMethod) RETURN resourceMethod").getColumn("resourceMethod"),
                hasItem(methodDescriptor(MyRestResource.class, "testGet")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept {@code jaxrs:PutResourceMethod}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void putResourceMethodConcept() throws Exception {
        scanClasses(MyRestResource.class);
        assertThat(applyConcept("jaxrs:PutResourceMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected PutResourceMethod", query("MATCH (resourceMethod:JaxRS:PutResourceMethod) RETURN resourceMethod").getColumn("resourceMethod"),
                hasItem(methodDescriptor(MyRestResource.class, "testPut")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept {@code jaxrs:PostResourceMethod}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void postResourceMethodConcept() throws Exception {
        scanClasses(MyRestResource.class);
        assertThat(applyConcept("jaxrs:PostResourceMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected PostResourceMethod", query("MATCH (resourceMethod:JaxRS:PostResourceMethod) RETURN resourceMethod").getColumn("resourceMethod"),
                hasItem(methodDescriptor(MyRestResource.class, "testPost", String.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept {@code jaxrs:DeleteResourceMethod}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void deleteResourceMethodConcept() throws Exception {
        scanClasses(MyRestResource.class);
        assertThat(applyConcept("jaxrs:DeleteResourceMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected DeleteResourceMethod", query("MATCH (resourceMethod:JaxRS:DeleteResourceMethod) RETURN resourceMethod")
                .getColumn("resourceMethod"), hasItem(methodDescriptor(MyRestResource.class, "testDelete")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept {@code jaxrs:HeadResourceMethod}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void headResourceMethodConcept() throws Exception {
        scanClasses(MyRestResource.class);
        assertThat(applyConcept("jaxrs:HeadResourceMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected HeadResourceMethod", query("MATCH (resourceMethod:JaxRS:HeadResourceMethod) RETURN resourceMethod").getColumn("resourceMethod"),
                hasItem(methodDescriptor(MyRestResource.class, "testHead")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept {@code jaxrs:OptionsResourceMethod}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void optionsResourceMethodConcept() throws Exception {
        scanClasses(MyRestResource.class);
        assertThat(applyConcept("jaxrs:OptionsResourceMethod").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected OptionsResourceMethod",
                query("MATCH (resourceMethod:JaxRS:OptionsResourceMethod) RETURN resourceMethod").getColumn("resourceMethod"),
                hasItem(methodDescriptor(MyRestResource.class, "testOptions")));
        store.commitTransaction();
    }

    /**
     * Verifies the concept {@code jaxrs:SubResourceLocator}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void subResourceLocatorConcept() throws Exception {
        scanClasses(MyRestResource.class);
        assertThat(applyConcept("jaxrs:SubResourceLocator").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected SubResourceLocator", query("MATCH (resourceMethod:JaxRS:SubResourceLocator) RETURN resourceMethod").getColumn("resourceMethod"),
                hasItem(methodDescriptor(MyRestResource.class, "getMySubResource", String.class)));
        assertThat("Expected SubResourceLocator", query("MATCH (resourceMethod:JaxRS:SubResourceLocator) RETURN resourceMethod").getColumn("resourceMethod"),
                not(hasItem(methodDescriptor(MyRestResource.class, "testGet"))));
        store.commitTransaction();
    }

    /**
     * Verifies the concept {@code jaxrs:SubResourceLocator} is not applied to
     * resource methods with entity parameters.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void invalidSubResourceLocatorConcept() throws Exception {
        scanClasses(MyRestResource.class);
        assertThat(applyConcept("jaxrs:SubResourceLocator").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected SubResourceLocator", query("MATCH (resourceMethod:JaxRS:SubResourceLocator) RETURN resourceMethod").getColumn("resourceMethod"),
                not(hasItem(methodDescriptor(MyRestResource.class, "getMyInvalidSubResource", String.class))));
        store.commitTransaction();
    }
}
