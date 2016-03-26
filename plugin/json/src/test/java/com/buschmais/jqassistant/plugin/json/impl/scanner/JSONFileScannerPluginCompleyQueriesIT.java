package com.buschmais.jqassistant.plugin.json.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugins.json.api.model.JSONKeyDescriptor;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;

public class JSONFileScannerPluginCompleyQueriesIT extends AbstractPluginIT {

    @Before
    public void startTransaction() {
        store.beginTransaction();
    }

    @After
    public void commitTransaction() {
        store.commitTransaction();
    }

    @Test
    public void scanReturnsFileDescriptorWithCorrectFileName() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginCompleyQueriesIT.class),
                                 "/probes/valid/true-false-null.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<?> results = query("MATCH (f:JSON:File) " +
                                "WHERE f.fileName =~ '.*/true-false-null.json' " +
                                "RETURN f"
        ).getColumn("f");

        assertThat(results, hasSize(1));
    }

    @Test
    public void scanReturnsObjectWithOneKeyValuePair() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginCompleyQueriesIT.class),
                                 "/probes/valid/object-one-key-value-pair.json");

        Scanner scanner = getScanner();
        scanner.scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<?> results = query("MATCH (f:JSON:File)-[:CONTAINS_DOCUMENT]->(d:JSON:Document) " +
                                     "-[:CONTAINS]->(o:JSON:Object)-[:HAS_KEY]->(k:Key:JSON) " +
                                     "-[:HAS_VALUE]->(v:Value) " +
                                     "WHERE k.name = 'A' AND v.value = 'B' " +
                                     "RETURN f"
        ).getColumn("f");

        assertThat(results, Matchers.notNullValue());
        assertThat(results, not(Matchers.empty()));
        assertThat(results, hasSize(1));
    }

    @Test
    public void scanReturnsObjectWithTwoKeyValuePairs() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginCompleyQueriesIT.class),
                                 "/probes/valid/object-two-key-value-pairs.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<?> results = query("MATCH (f:JSON:File)-[:CONTAINS_DOCUMENT]->(d:JSON:Document) " +
                                "-[:CONTAINS]->(o:JSON:Object)-[:HAS_KEY]->(k:Key:JSON) " +
                                "-[:HAS_VALUE]->(v:Value) " +
                                "WHERE k.name = 'A' AND v.value = 'B' " +
                                "RETURN f"
        ).getColumn("f");

        assertThat(results, hasSize(1));
    }

    @Test
    public void scanReturnsObjectWithThreeKeysWithTrueFalseAndNullValue() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginCompleyQueriesIT.class),
                                 "/probes/valid/true-false-null.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<?> results = query("MATCH (f:JSON:File)-[:CONTAINS_DOCUMENT]->(d:JSON:Document) " +
                                "-[:CONTAINS]->(o:JSON:Object)-[:HAS_KEY]->(k:Key:JSON) " +
                                "WHERE " +
                                "(k.name = 'A') OR " +
                                "(k.name = 'B') OR " +
                                "(k.name = 'C') " +
                                "RETURN k"
        ).getColumn("k");

        assertThat(results, hasSize(3));
    }


    @Test
    public void scanReturnsObjectThereOneValueIsNull() {
        File jsonFile = new File(getClassesDirectory(JSONFileScannerPluginCompleyQueriesIT.class),
                                 "/probes/valid/true-false-null.json");

        getScanner().scan(jsonFile, jsonFile.getAbsolutePath(), null);

        List<JSONKeyDescriptor> results = query("MATCH (f:JSON:File)-[:CONTAINS_DOCUMENT]->(d:JSON:Document) " +
                                                "-[:CONTAINS]->(o:JSON:Object)-[:HAS_KEY]->(k:Key:JSON) " +
                                                "WHERE " +
                                                "NOT(k-[:HAS_VALUE]->()) " +
                                                "RETURN k"
        ).getColumn("k");

        assertThat(results, hasSize(1));

        JSONKeyDescriptor keyDescriptor = results.get(0);

        assertThat(keyDescriptor.getName(), equalTo("C"));
    }


    private JSONKeyDescriptor findKeyInDocument(List<JSONKeyDescriptor> keys, String name) {
        JSONKeyDescriptor result = null;

        for (JSONKeyDescriptor key : keys) {
            if (key.getName().equals(name)) {
                result = key;
                break;
            }
        }

        return result;
    }
}
