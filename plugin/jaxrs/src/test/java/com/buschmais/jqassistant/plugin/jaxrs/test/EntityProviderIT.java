package com.buschmais.jqassistant.plugin.jaxrs.test;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status.SUCCESS;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.jaxrs.test.set.beans.BookReader;
import com.buschmais.jqassistant.plugin.jaxrs.test.set.beans.BookWriter;

/**
 * Test to verify JAX-RS Entity Provider concepts.
 * 
 * @author Aparna Chaudhary
 */
public class EntityProviderIT extends AbstractJavaPluginIT {

    /**
     * Verifies the concept {@code jaxrs:EntityProvider} for
     * {@link MessageBodyWriter}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void test_EntityProvider_Writer_Concept() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(BookWriter.class);
        assertThat(applyConcept("jaxrs:EntityProvider").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected entityProvider", query("MATCH (entityProvider:JaxRS:EntityProvider) RETURN entityProvider").getColumn("entityProvider"),
                hasItem(typeDescriptor(BookWriter.class)));
        store.commitTransaction();
    }

    /**
     * Verifies the concept {@code jaxrs:EntityProvider} for
     * {@link MessageBodyReader}.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     * @throws NoSuchMethodException
     *             If the test fails.
     */
    @Test
    public void test_EntityProvider_Reader_Concept() throws IOException, AnalysisException, NoSuchMethodException {
        scanClasses(BookReader.class);
        assertThat(applyConcept("jaxrs:EntityProvider").getStatus(), equalTo(SUCCESS));
        store.beginTransaction();
        assertThat("Expected entityProvider", query("MATCH (entityProvider:JaxRS:EntityProvider) RETURN entityProvider").getColumn("entityProvider"),
                hasItem(typeDescriptor(BookReader.class)));
        store.commitTransaction();
    }
}
