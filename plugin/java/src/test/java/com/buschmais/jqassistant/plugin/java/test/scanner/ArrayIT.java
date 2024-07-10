package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.array.Array;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.assertj.FieldDescriptorCondition.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.assertj.MethodDescriptorCondition.methodDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

class ArrayIT extends AbstractJavaPluginIT {

    @Test
    void field() throws ReflectiveOperationException {
        scanClasses(Array.class);
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("className", Array.class.getName());
        TestResult testResult = query("MATCH (t:Type)-[:DECLARES]->(f:Field) WHERE t.fqn=$className RETURN f", parameters);
        assertThat(testResult.<FieldDescriptor>getColumn("f")).haveExactly(1, fieldDescriptor(Array.class, "stringArray"));
        testResult = query("MATCH (t:Type)-[:DECLARES]->(m:Method) WHERE t.fqn=$className RETURN m", parameters);
        assertThat(testResult.<MethodDescriptor>getColumn("m")).haveExactly(1, methodDescriptor(Array.class, "getStringArray"))
            .haveExactly(1, methodDescriptor(Array.class, "getStringArray"));
        store.commitTransaction();
    }
}
