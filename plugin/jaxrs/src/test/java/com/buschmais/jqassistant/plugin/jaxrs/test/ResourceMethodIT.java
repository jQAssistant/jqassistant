package com.buschmais.jqassistant.plugin.jaxrs.test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.jaxrs.test.set.beans.MyRestResource;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 * Test to verify REST Resource method concepts.
 * 
 * @author Aparna Chaudhary
 */
public class ResourceMethodIT extends AbstractPluginIT {

	/**
	 * Verifies the concept {@code rest:GetResourceMethod}.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws AnalyzerException
	 *             If the test fails.
	 * @throws NoSuchMethodException
	 *             If the test fails.
	 */
	@Test
	public void test_GetResourceMethod_Concept() throws IOException, AnalyzerException, NoSuchMethodException {
		scanClasses(MyRestResource.class);
		applyConcept("jaxrs:GetResourceMethod");
		store.beginTransaction();
		assertThat("Expected GetResourceMethod",
				query("MATCH (restMethod:JaxRS:GetResourceMethod) RETURN restMethod").getColumn("restMethod"),
				hasItem(methodDescriptor(MyRestResource.class, "testGet")));
		store.commitTransaction();
	}

	/**
	 * Verifies the concept {@code rest:PutResourceMethod}.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws AnalyzerException
	 *             If the test fails.
	 * @throws NoSuchMethodException
	 *             If the test fails.
	 */
	@Test
	public void test_PutResourceMethod_Concept() throws IOException, AnalyzerException, NoSuchMethodException {
		scanClasses(MyRestResource.class);
		applyConcept("jaxrs:PutResourceMethod");
		store.beginTransaction();
		assertThat("Expected PutResourceMethod",
				query("MATCH (restMethod:JaxRS:PutResourceMethod) RETURN restMethod").getColumn("restMethod"),
				hasItem(methodDescriptor(MyRestResource.class, "testPut")));
		store.commitTransaction();
	}

	/**
	 * Verifies the concept {@code rest:PostResourceMethod}.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws AnalyzerException
	 *             If the test fails.
	 * @throws NoSuchMethodException
	 *             If the test fails.
	 */
	@Test
	public void test_PostResourceMethod_Concept() throws IOException, AnalyzerException, NoSuchMethodException {
		scanClasses(MyRestResource.class);
		applyConcept("jaxrs:PostResourceMethod");
		store.beginTransaction();
		assertThat("Expected PostResourceMethod",
				query("MATCH (restMethod:JaxRS:PostResourceMethod) RETURN restMethod").getColumn("restMethod"),
				hasItem(methodDescriptor(MyRestResource.class, "testPost", String.class)));
		store.commitTransaction();
	}

	/**
	 * Verifies the concept {@code rest:DeleteResourceMethod}.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws AnalyzerException
	 *             If the test fails.
	 * @throws NoSuchMethodException
	 *             If the test fails.
	 */
	@Test
	public void test_DeleteResourceMethod_Concept() throws IOException, AnalyzerException, NoSuchMethodException {
		scanClasses(MyRestResource.class);
		applyConcept("jaxrs:DeleteResourceMethod");
		store.beginTransaction();
		assertThat("Expected DeleteResourceMethod",
				query("MATCH (restMethod:JaxRS:DeleteResourceMethod) RETURN restMethod").getColumn("restMethod"),
				hasItem(methodDescriptor(MyRestResource.class, "testDelete")));
		store.commitTransaction();
	}

	/**
	 * Verifies the concept {@code rest:HeadResourceMethod}.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws AnalyzerException
	 *             If the test fails.
	 * @throws NoSuchMethodException
	 *             If the test fails.
	 */
	@Test
	public void test_HeadResourceMethod_Concept() throws IOException, AnalyzerException, NoSuchMethodException {
		scanClasses(MyRestResource.class);
		applyConcept("jaxrs:HeadResourceMethod");
		store.beginTransaction();
		assertThat("Expected HeadResourceMethod",
				query("MATCH (restMethod:JaxRS:HeadResourceMethod) RETURN restMethod").getColumn("restMethod"),
				hasItem(methodDescriptor(MyRestResource.class, "testHead")));
		store.commitTransaction();
	}

	/**
	 * Verifies the concept {@code rest:OptionsResourceMethod}.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws AnalyzerException
	 *             If the test fails.
	 * @throws NoSuchMethodException
	 *             If the test fails.
	 */
	@Test
	public void test_OptionsResourceMethod_Concept() throws IOException, AnalyzerException, NoSuchMethodException {
		scanClasses(MyRestResource.class);
		applyConcept("jaxrs:OptionsResourceMethod");
		store.beginTransaction();
		assertThat("Expected OptionsResourceMethod",
				query("MATCH (restMethod:JaxRS:OptionsResourceMethod) RETURN restMethod").getColumn("restMethod"),
				hasItem(methodDescriptor(MyRestResource.class, "testOptions")));
		store.commitTransaction();
	}
}
