package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.anchor;

import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12.AbstractYAMLPluginIT;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AliasIT extends AbstractYAMLPluginIT {
    @Test
    void cypherAnchorForScalar() {
        readSourceDocument("/anchor/toplevel-sequence-anchor-on-scalar.yml");

        String cypherQuery = "MATCH (alias:Yaml:Alias {value: 'bbbbb'}) " +
                             "      -[:IS_ALIAS_FOR]-> " +
                             "      (anchor:Yaml:Anchor {value: 'bbbbb', anchorName: 'alias'}) " +
                             "RETURN alias, anchor";

        TestResult testResult = query(cypherQuery);

        assertThat(testResult.getColumn("alias")).hasSize(1);
        assertThat(testResult.getColumn("anchor")).hasSize(1);
    }

    @Test
    void cypherAnchorForMap() {
        readSourceDocument("/anchor/toplevel-map-anchor-on-map.yml");

        String cypherQuery = "MATCH (alias:Yaml:Alias:Map) " +
                             "      -[:IS_ALIAS_FOR]-> " +
                             "      (anchor:Yaml:Anchor:Map) " +
                             "RETURN alias, anchor";

        TestResult testResult = query(cypherQuery);

        assertThat(testResult.getColumn("alias")).hasSize(1);
        assertThat(testResult.getColumn("anchor")).hasSize(1);
    }

    @Test
    void cypherAnchorOnScalarAndAliasInMap() {
        readSourceDocument("/anchor/toplevel-sequence-anchor-on-scalar-alias-in-map.yml");

        String cypherQuery = "MATCH (alias:Yaml:Alias:Scalar {value: 'dd'}) " +
                             "      -[:IS_ALIAS_FOR]-> " +
                             "      (anchor:Yaml:Anchor:Scalar {value: 'dd'}) " +
                             "RETURN alias, anchor";

        TestResult testResult = query(cypherQuery);

        assertThat(testResult.getColumn("alias")).hasSize(1);
        assertThat(testResult.getColumn("anchor")).hasSize(1);
    }

    @Disabled("See https://github.com/jQAssistant/jqa-yaml2-plugin/issues/5")
    @Test
    void cypherAnchorInComplexKey() {
        readSourceDocument("/anchor/toplevel-map-anchor-in-complexkey.yml");

        String cypherQuery = "MATCH (alias:Yaml:Alias)   " +
                             "      -[:IS_ALIAS_FOR]->   " +
                             "      (anchor:Yaml:Anchor) " +
                             "RETURN alias, anchor       ";

        TestResult testResult = query(cypherQuery);

        assertThat(testResult.getColumn("alias")).hasSize(1);
        assertThat(testResult.getColumn("anchor")).hasSize(1);
    }
}
