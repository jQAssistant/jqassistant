package com.buschmais.jqassistant.plugin.cdi.test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.event.CustomEventConsumer;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.event.CustomEventProducer;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

/**
 * Tests for CDI event concepts.
 * 
 * @author Aparna Chaudhary
 */
public class CdiEventIT extends AbstractJavaPluginIT {

	/**
	 * Verifies the concept "cdi:EventProducer".
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
	 *             If the test fails.
	 */
	@Test
	public void test_EventProducer_Concept() throws IOException, AnalysisException {
		scanClasses(CustomEventProducer.class);
        assertThat(applyConcept("cdi:EventProducer").getStatus(), equalTo(Result.Status.SUCCESS));
		store.beginTransaction();
		assertThat("Expected EventProducer", query("MATCH (e:Type:Cdi:EventProducer) RETURN e").getColumn("e"), hasItem(typeDescriptor(CustomEventProducer.class)));
		store.commitTransaction();
	}

	/**
	 * Verifies the concept {@code cdi:EventProducer} is not applied to invalid EventProducer classes.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
	 *             If the test fails.
	 */
	@Test
	public void testInvalid_EventProducer_Concept() throws IOException, AnalysisException {
		scanClasses(CdiEventIT.class);
        assertThat(applyConcept("cdi:EventProducer").getStatus(), equalTo(Result.Status.FAILURE));
		store.beginTransaction();
		assertThat("Unexpected EventProducer", query("MATCH (e:Type:Cdi:EventProducer) RETURN e").getColumn("e"), nullValue());
		store.commitTransaction();
	}

	/**
	 * Verifies the concept "cdi:EventConsumer".
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
	 *             If the test fails.
	 */
	@Test
	public void test_EventConsumer_Concept() throws IOException, AnalysisException {
		scanClasses(CustomEventConsumer.class);
		assertThat(applyConcept("cdi:EventConsumer").getStatus(), equalTo(Result.Status.SUCCESS));
		store.beginTransaction();
		assertThat("Expected EventConsumer", query("MATCH (e:Type:Cdi:EventConsumer) RETURN e").getColumn("e"), hasItem(typeDescriptor(CustomEventConsumer.class)));
		store.commitTransaction();
	}

	/**
	 * Verifies the concept {@code cdi:EventConsumer} is not applied to invalid EventConsumer classes.
	 * 
	 * @throws java.io.IOException
	 *             If the test fails.
	 * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
	 *             If the test fails.
	 */
	@Test
	public void testInvalid_EventConsumer_Concept() throws IOException, AnalysisException {
		scanClasses(CdiEventIT.class);
        assertThat(applyConcept("cdi:EventConsumer").getStatus(), equalTo(Result.Status.FAILURE));
		store.beginTransaction();
		assertThat("Unexpected EventConsumer", query("MATCH (e:Type:Cdi:EventConsumer) RETURN e").getColumn("e"), nullValue());
		store.commitTransaction();
	}
}
