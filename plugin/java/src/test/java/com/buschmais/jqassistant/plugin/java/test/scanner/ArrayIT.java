package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.array.Array;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

public class ArrayIT extends AbstractJavaPluginIT {

    @Test
    public void field() throws ReflectiveOperationException {
        scanClasses(Array.class);
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("className", Array.class.getName());
        TestResult testResult = query("MATCH (t:Type)-[:DECLARES]->(f:Field) WHERE t.fqn=$className RETURN f", parameters);
        assertThat(testResult.getColumn("f"), hasItem(fieldDescriptor(Array.class, "stringArray")));
        testResult = query("MATCH (t:Type)-[:DECLARES]->(m:Method) WHERE t.fqn=$className RETURN m", parameters);
        assertThat(testResult.getColumn("m"), hasItems(methodDescriptor(Array.class, "getStringArray"), methodDescriptor(Array.class, "getStringArray")));
        store.commitTransaction();
    }
}
