package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.array.Array;

public class ArrayIT extends AbstractPluginIT {

	@Test
	public void field() throws IOException, NoSuchFieldException, NoSuchMethodException {
		scanClasses(Array.class);
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("className", Array.class.getName());
		TestResult testResult = query("MATCH (t:TYPE)-[:CONTAINS]->(f:FIELD) WHERE t.FQN={className} RETURN f", parameters);
		assertThat(testResult.getColumn("f"), hasItem(fieldDescriptor(Array.class, "stringArray")));
		testResult = query("MATCH (t:TYPE)-[:CONTAINS]->(m:METHOD) WHERE t.FQN={className} RETURN m", parameters);
		assertThat(testResult.getColumn("m"),
				allOf(hasItem(methodDescriptor(Array.class, "getStringArray")), hasItem(methodDescriptor(Array.class, "getStringArray"))));
	}
}
