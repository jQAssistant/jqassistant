package com.buschmais.jqassistant.plugin.java.test.rules;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.ClassType;
import com.buschmais.jqassistant.plugin.java.test.set.rules.java.InterfaceType;

/**
 * Tests for the concept java:AssignableFrom.
 */
public class AssignableFromIT extends AbstractPluginIT {

	/**
	 * Verifies the concept "java:AssignableFrom".
	 * 
	 * @throws IOException
	 *             If the test fails.
	 * @throws AnalyzerException
	 *             If the test fails.
	 */
	@Test
	public void assignableFrom() throws IOException, AnalyzerException {
		scanClasses(ClassType.class);
		applyConcept("java:AssignableFrom");
        store.beginTransaction();
		assertThat(
				query("MATCH (types:TYPE)<-[:ASSIGNABLE_FROM]-(assignableType) RETURN assignableType").getColumn("assignableType"),
				allOf(hasItem(typeDescriptor(ClassType.class)), hasItem(typeDescriptor(InterfaceType.class)),
						hasItem(typeDescriptor(Object.class))));
        store.commitTransaction();
	}
}
