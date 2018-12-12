package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.fieldvalue.FieldValue;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Contains test which verify correct scanning static field values.
 */
public class FieldValueIT extends AbstractJavaPluginIT {

    /**
     * Verifies an annotation on class level.
     * 
     * @throws IOException
     *             If the test fails.
     */
    @Test
    public void fieldValues() throws IOException {
        scanClasses(FieldValue.class);
        store.beginTransaction();
        verifyValue("stringValue", "StringValue");
        verifyValue("intValue", Integer.valueOf(1));
        store.commitTransaction();
    }

    private <V> void verifyValue(String fieldName, V expectedValue) {
        Map<String, Object> params = MapBuilder.<String, Object> create("fieldName", fieldName).get();
        TestResult testResult = query("MATCH (:Type)-[:DECLARES]->(f:Field)-[:HAS]->(v:Value:Primitive) WHERE f.name={fieldName} RETURN v.value as value",
                params);
        List<Map<String, Object>> rows = testResult.getRows();
        assertThat(rows.size(), equalTo(1));
        Map<String, Object> row = rows.get(0);
        V value = (V) row.get("value");
        assertThat(value, equalTo(expectedValue));
    }
}
