package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.issues;

import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12.AbstractYAMLPluginIT;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GH5AnchorsInAComplexKeyIT extends AbstractYAMLPluginIT {

    @TestStore(reset = false, type = TestStore.Type.MEMORY)
    @Test
    void anchorOnlyInAComplexKey() {
        readSourceDocument("/anchorincomplexkey/anchor-only.yaml");

        String query = "MATCH (n:Anchor) RETURN n";

        TestResult testResult = query(query);
        assertThat(testResult.getColumns()).containsKeys("n");
    }

    @TestStore(reset = false, type = TestStore.Type.MEMORY)
    @Test
    void anchorInKeyAndAliasAsValue() {
        readSourceDocument("/anchorincomplexkey/anchor-and-alias.yaml");

        String query = "MATCH (n:Anchor) RETURN n";

        TestResult testResult = query(query);
        assertThat(testResult.getColumns()).containsKeys("n");
    }


}
