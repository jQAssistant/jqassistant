package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.scanner.test.set.array.Array;
import com.buschmais.jqassistant.store.api.QueryResult;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.buschmais.jqassistant.scanner.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.scanner.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static org.junit.Assert.assertThat;

public class ArrayIT extends AbstractScannerIT {

    @Test
    public void field() throws IOException, NoSuchFieldException, NoSuchMethodException {
        scanClasses(Array.class);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("className", Array.class.getName());
        QueryResult result = store.executeQuery("MATCH (c:CLASS)-[:CONTAINS]->(f:FIELD) WHERE c.FQN={className} RETURN f", parameters);
        FieldDescriptor field = result.getRows().iterator().next().get("f");
        assertThat(field, fieldDescriptor(Array.class.getDeclaredField("stringArray")));

        result = store.executeQuery("MATCH (c:CLASS)-[:CONTAINS]->(m:METHOD) WHERE c.FQN={className} RETURN m ORDER BY m.FQN", parameters);
        Iterator<QueryResult.Row> iterator = result.getRows().iterator();
        MethodDescriptor method = iterator.next().get("m");
        assertThat(method, methodDescriptor(Array.class.getDeclaredMethod("getStringArray")));
        // skip constructor
        iterator.next().get("m");
        method = iterator.next().get("m");
        assertThat(method, methodDescriptor(Array.class.getDeclaredMethod("setStringArray", String[].class)));
    }
}
