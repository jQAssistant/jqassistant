package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.test.set.array.Array;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.buschmais.jqassistant.scanner.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.scanner.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

public class ArrayIT extends AbstractScannerIT {

    @Test
    public void field() throws IOException, NoSuchFieldException, NoSuchMethodException {
        scanClasses(Array.class);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("className", Array.class.getName());
        TestResult testResult = executeQuery("MATCH (c:CLASS)-[:CONTAINS]->(f:FIELD) WHERE c.FQN={className} RETURN f", parameters);
        assertThat(testResult.getColumns().get("f"), hasItem(fieldDescriptor(Array.class, "stringArray")));
        testResult = executeQuery("MATCH (c:CLASS)-[:CONTAINS]->(m:METHOD) WHERE c.FQN={className} RETURN m", parameters);
        assertThat(testResult.getColumns().get("m"), allOf(hasItem(methodDescriptor(Array.class, "getStringArray")), hasItem(methodDescriptor(Array.class, "getStringArray"))));
    }
}
