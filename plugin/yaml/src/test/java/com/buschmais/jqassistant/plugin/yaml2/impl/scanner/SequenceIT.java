package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12.AbstractYAMLPluginIT;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "jqassistant.yaml2.activate", matches = "^true$")
public class SequenceIT extends AbstractYAMLPluginIT {
    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        store.commitTransaction();
    }

    @Test
    void eachItemOfASequenceHasTheLabelItemForEachItemInDocument() {
        readSourceDocument("/sequences/five-items.yaml");

        String query = "MATCH (item:Yaml:Item) " +
                       "RETURN item";

        List<YMLScalarDescriptor> result = query(query).getColumn("item");

        assertThat(result).hasSize(6);
    }


    @Test
    void eachItemOfTheToplevelSequenceHasTheLabelItem() {
        readSourceDocument("/sequences/five-items.yaml");

        String query = "MATCH (:Document)-->(:Sequence)-->(item:Yaml:Item) " +
                       "RETURN item";

        List<YMLScalarDescriptor> result = query(query).getColumn("item");

        assertThat(result).hasSize(5);
    }

}
