package com.buschmais.jqassistant.plugin.cdi.test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.plugin.cdi.test.set.beans.Bean;
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
		scanClasses(Bean.class);
		scanClasses(CustomEventProducer.class);
		applyConcept("cdi:EventProducer");
		store.beginTransaction();
		assertThat(query("MATCH (e:Type:Cdi:EventProducer) RETURN e").getColumn("e"), hasItem(typeDescriptor(CustomEventProducer.class)));
		store.commitTransaction();
	}
	
    /**
     * Verifies the concept {@code cdi:EventProducer} is not applied to invalid
     * EventProducer classes.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the test fails.
     */
    @Test
    public void testInvalid_EventProducer_Concept() throws IOException, AnalysisException {
		scanClasses(CdiEventIT.class);
		applyConcept("cdi:EventProducer");
        store.beginTransaction();
        assertThat("Unexpected EventProducer", query("MATCH (e:Type:Cdi:EventProducer) RETURN e").getColumn("e"), nullValue());
        store.commitTransaction();

    }	
}
