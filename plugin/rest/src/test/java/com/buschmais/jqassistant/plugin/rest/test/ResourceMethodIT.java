package com.buschmais.jqassistant.plugin.rest.test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.rest.test.set.beans.MyRestResource;

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
		applyConcept("rest:GetResourceMethod");
		store.beginTransaction();
		assertThat("Expected GetResourceMethod",
				query("MATCH (restMethod:Rest:GetResourceMethod) RETURN restMethod").getColumn("restMethod"),
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
		applyConcept("rest:PutResourceMethod");
		store.beginTransaction();
		assertThat("Expected PutResourceMethod",
				query("MATCH (restMethod:Rest:PutResourceMethod) RETURN restMethod").getColumn("restMethod"),
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
		applyConcept("rest:PostResourceMethod");
		store.beginTransaction();
		assertThat("Expected PostResourceMethod",
				query("MATCH (restMethod:Rest:PostResourceMethod) RETURN restMethod").getColumn("restMethod"),
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
		applyConcept("rest:DeleteResourceMethod");
		store.beginTransaction();
		assertThat("Expected DeleteResourceMethod",
				query("MATCH (restMethod:Rest:DeleteResourceMethod) RETURN restMethod").getColumn("restMethod"),
				hasItem(methodDescriptor(MyRestResource.class, "testDelete")));
		store.commitTransaction();
	}
}
