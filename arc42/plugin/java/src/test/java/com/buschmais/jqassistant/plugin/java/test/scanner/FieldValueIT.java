package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.shared.map.MapBuilder;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.fieldvalue.FieldValue;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains test which verify correct scanning static field values.
 */
class FieldValueIT extends AbstractJavaPluginIT {

    /**
     * Verifies an annotation on class level.
     *
     */
    @Test
    void fieldValues() {
        scanClasses(FieldValue.class);
        store.beginTransaction();
        assertThat(getFieldValue("stringValue", String.class)).isEqualTo("StringValue");
        assertThat(getFieldValue("longValue", Number.class).longValue()).isEqualTo(1l);
        store.commitTransaction();
    }

    private <V> V getFieldValue(String fieldName, Class<V> type) {
        Map<String, Object> params = MapBuilder.<String, Object> builder().entry("fieldName", fieldName).build();
        TestResult testResult = query("MATCH (:Type)-[:DECLARES]->(f:Field)-[:HAS]->(v:Value:Primitive) WHERE f.name=$fieldName RETURN v.value as value",
                params);
        List<Map<String, Object>> rows = testResult.getRows();
        assertThat(rows.size()).isEqualTo(1);
        Map<String, Object> row = rows.get(0);
        return type.cast(row.get("value"));
    }
}
