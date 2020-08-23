package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.anchor;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12.AbstractYAMLPluginIT;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnchorAndAliasIT extends AbstractYAMLPluginIT {
    @Test
    void cypherRelationBetweenAliasAndAnchorIsCorrectForFirstAlias() {
        readSourceDocument("/anchor/map-with-alias.yml");

        String query = "MATCH\n" +
                           "  (:Sequence)-[:HAS_ITEM]->(:Map)-[:HAS_KEY]->\n" +
                           "    (k1:Key:Simple)-[:HAS_VALUE]->\n" +
                           "      (v1:Value:Anchor {value: 'Emilia', anchorName: 'name'}),\n" +
                           "\n" +
                           "  (:Sequence)-[:HAS_ITEM]->(:Map)-[:HAS_KEY]->\n" +
                           "    (k2:Key:Simple)-[:HAS_VALUE]->\n" +
                           "      (v2:Value {value: 'Emilia'})\n" +
                           "\n" +
                           "WHERE\n" +
                           "  NOT v2:Anchor\n" +
                           "\n" +
                           "WITH v1, v2\n" +
                           "\n" +
                           "MATCH\n" +
                           "    (v2)-[relation]->(v1)\n" +
                           "\n" +
                           "RETURN\n" +
                           "  type(relation) AS typeName";

        List<Object> typeNames = query(query).getColumn("typeName");

        assertThat(typeNames).containsOnly("IS_ALIAS_FOR");
    }

    @Test
    void cypherRelationBetweenAliasAndAnchorIsCorrectForSecondAlias() {
        readSourceDocument("/anchor/map-with-alias.yml");

        String query = "MATCH\n" +
                       "  (:Sequence)-[:HAS_ITEM]->(:Map)-[:HAS_KEY]->\n" +
                       "    (k1:Key:Simple)-[:HAS_VALUE]->\n" +
                       "      (v1:Value:Anchor {value: 'Elias', anchorName: 'name'}),\n" +
                       "\n" +
                       "  (:Sequence)-[:HAS_ITEM]->(:Map)-[:HAS_KEY]->\n" +
                       "    (k2:Key:Simple)-[:HAS_VALUE]->\n" +
                       "      (v2:Value {value: 'Elias'})\n" +
                       "\n" +
                       "WHERE\n" +
                       "  NOT v2:Anchor\n" +
                       "\n" +
                       "WITH v1, v2\n" +
                       "\n" +
                       "MATCH\n" +
                       "    (v2)-[relation]->(v1)\n" +
                       "\n" +
                       "RETURN\n" +
                       "  type(relation) AS typeName";

        List<Object> typeNames = query(query).getColumn("typeName");

        assertThat(typeNames).containsOnly("IS_ALIAS_FOR");
    }
}
