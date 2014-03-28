package com.buschmais.jqassistant.plugin.jaxrs.test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.jaxrs.test.set.beans.BookReader;
import com.buschmais.jqassistant.plugin.jaxrs.test.set.beans.BookWriter;

/**
 * Test to verify JAX-RS Entity Provider concepts.
 * 
 * @author Aparna Chaudhary
 */
public class EntityProviderIT extends AbstractPluginIT {

	/**
	 * Verifies the concept {@code jaxrs:EntityProvider} for {@link MessageBodyWriter}.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws AnalyzerException
	 *             If the test fails.
	 * @throws NoSuchMethodException
	 *             If the test fails.
	 */
	@Test
	public void test_EntityProvider_Writer_Concept() throws IOException, AnalyzerException, NoSuchMethodException {
		scanClasses(BookWriter.class);
		applyConcept("jaxrs:EntityProvider");
		store.beginTransaction();
		assertThat("Expected entityProvider",
				query("MATCH (entityProvider:JaxRS:EntityProvider) RETURN entityProvider").getColumn("entityProvider"),
				hasItem(typeDescriptor(BookWriter.class)));
		store.commitTransaction();
	}

	/**
	 * Verifies the concept {@code jaxrs:EntityProvider} for {@link MessageBodyReader}.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws AnalyzerException
	 *             If the test fails.
	 * @throws NoSuchMethodException
	 *             If the test fails.
	 */
	@Test
	public void test_EntityProvider_Reader_Concept() throws IOException, AnalyzerException, NoSuchMethodException {
		scanClasses(BookReader.class);
		applyConcept("jaxrs:EntityProvider");
		store.beginTransaction();
		assertThat("Expected entityProvider",
				query("MATCH (entityProvider:JaxRS:EntityProvider) RETURN entityProvider").getColumn("entityProvider"),
				hasItem(typeDescriptor(BookReader.class)));
		store.commitTransaction();
	}
}
