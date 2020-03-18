package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12.AbstractYAMLPluginIT;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NullValueHandlingIT extends AbstractYAMLPluginIT {


    @Test
    void propertyValueIsAlwaysPresetIfValueForANodeIsMissing() {
        readSourceDocument("/nullvalue/010-null-value-in-list.yaml");


        String query = "MATCH (:Yaml:Sequence)-[HAS_ITEM]->(item:Yaml:Scalar) " +
                       "WHERE EXISTS(item.value) " +
                       "RETURN item";

        TestResult testResult = query(query);
        assertThat(testResult.getColumns()).containsKeys("item");

        List<YMLScalarDescriptor> result = testResult.getColumn("item");

        assertThat(result).hasSize(3);
    }

    @Test
    void valueOfAMissingContentForANodeMustBeAnEmptyString() {
        readSourceDocument("/nullvalue/010-null-value-in-list.yaml");


        String query = "MATCH (:Yaml:Sequence)-[HAS_ITEM]->(item:Yaml:Scalar { value: '' }) " +
                       "RETURN item";

        List<YMLScalarDescriptor> result = query(query).getColumn("item");

        assertThat(result).hasSize(1);
    }
}
