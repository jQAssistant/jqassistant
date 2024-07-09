package com.buschmais.jqassistant.plugin.json.impl.scanner;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.json.api.model.JSONDescriptor;
import com.buschmais.jqassistant.plugin.json.api.model.JSONScalarValueDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JSONFileScannerPluginQueriesIT extends AbstractPluginIT {

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        if (store.hasActiveTransaction()) {
            store.commitTransaction();
        }
    }

    @Test
    void scanReturnsFileDescriptorWithCorrectFileName() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginQueriesIT.class),
                                 "/probes/valid/true-false-null.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<?> results = query("MATCH (f:Json:File) " +
                                "WHERE f.fileName =~ '.*/true-false-null.json' " +
                                "RETURN f"
        ).getColumn("f");
        assertThat(results).hasSize(1);
    }

    @Test
    void scanReturnsObjectWithOneKeyValuePair() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginQueriesIT.class),
                                 "/probes/valid/object-one-key-value-pair.json");

        Scanner scanner = getScanner();
        scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<?> results = query("MATCH (f:Json:File) " +
                                     "-[:CONTAINS]->(o:Json:Object)-[:HAS_KEY]->(k:Key:Json) " +
                                     "-[:HAS_VALUE]->(v:Value) " +
                                     "WHERE k.name = 'A' AND v.value = 'B' " +
                                     "RETURN f"
        ).getColumn("f");

        assertThat(results).isNotNull();
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
    }

    @Test
    void scanReturnsObjectWithTwoKeyValuePairs() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginQueriesIT.class),
                                 "/probes/valid/object-two-key-value-pairs.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<?> results = query("MATCH (f:Json:File)" +
                                "-[:CONTAINS]->(o:Json:Object)-[:HAS_KEY]->(k:Key:Json) " +
                                "-[:HAS_VALUE]->(v:Value) " +
                                "WHERE k.name = 'A' AND v.value = 'B' " +
                                "RETURN f"
        ).getColumn("f");

        assertThat(results).hasSize(1);
    }

    @Test
    void scanReturnsObjectWithThreeKeysWithTrueFalseAndNullValue() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginQueriesIT.class),
                                 "/probes/valid/true-false-null.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<?> results = query("MATCH (f:Json:File)" +
                                "-[:CONTAINS]->(o:Json:Object)-[:HAS_KEY]->(k:Key:Json) " +
                                "WHERE " +
                                "(k.name = 'A') OR " +
                                "(k.name = 'B') OR " +
                                "(k.name = 'C') " +
                                "RETURN k"
        ).getColumn("k");

        assertThat(results).hasSize(3);
    }


    @Test
    void scanReturnsObjectThereOneValueIsNull() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginQueriesIT.class),
                                 "/probes/valid/true-false-null.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<Map<String, Object>> rows = query("MATCH (f:Json:File) " +
            "-[:CONTAINS]->(o:Json:Object)-[:HAS_KEY]->(k:Key:Json) " +
            "WHERE " +
            "NOT ((k)-[:HAS_VALUE]->()) " +
            "RETURN k"
        ).getRows();

        assertThat(rows).isEmpty();
    }


    @Test
    void scanReturnsSingleInteger() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginQueriesIT.class),
                                 "/probes/valid/single-int.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<JSONScalarValueDescriptor> results = query("MATCH (f:Json:File) " +
                                                        "-[:CONTAINS]->(o:Json) " +
                                                        "WHERE o.value = 123 " +
                                                        "RETURN o"
        ).getColumn("o");
        assertThat(results).hasSize(1);
        JSONScalarValueDescriptor valueDescriptor= results.get(0);
        assertThat(valueDescriptor.getValue()).isEqualTo(123);
    }

    @Test
    void rootObjectIsNotAValueOthersAre() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginQueriesIT.class),
                "/probes/valid/object-with-object-empty.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        // Get the first array
        List<JSONDescriptor> resultsA = query("MATCH (f:Json:File) " +
                "-[:CONTAINS]->(o:Json:Object) " +
                "RETURN o"
        ).getColumn("o");

        // Get the inferior arrays
        List<JSONDescriptor> resultsB = query("MATCH (f:Json:File) " +
                "-[:CONTAINS]->(:Json:Object)-[*2]->(o:Json) " +
                "WHERE o:Value " +
                "RETURN o"
        ).getColumn("o");

        assertThat(resultsA).hasSize(1);
        assertThat(resultsB).hasSize(1);
    }

    @Test
    void rootArrayIsNotAValueOthersAre() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginQueriesIT.class),
                "/probes/valid/array-of-arrays.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        // Get the first array
        List<JSONDescriptor> resultsA = query("MATCH (f:Json:File) " +
                "-[:CONTAINS]->(a:Json:Array) " +
          //      "WHERE NOT a:Value " +
                "RETURN a"
        ).getColumn("a");

        // Get the inferior arrays
        List<JSONDescriptor> resultsB = query("MATCH (f:Json:File) " +
                "-[:CONTAINS]->(:Json:Array)-[:CONTAINS_VALUE]->(a:Json) " +
                "WHERE a:Value " +
                "RETURN a"
        ).getColumn("a");

        assertThat(resultsA).hasSize(1);
        assertThat(resultsB).hasSize(3);
    }

}
