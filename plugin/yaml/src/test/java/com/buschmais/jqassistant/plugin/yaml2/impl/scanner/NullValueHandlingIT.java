package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12.AbstractYAMLPluginIT;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

public class NullValueHandlingIT extends AbstractYAMLPluginIT {


    @Test
    void propertyValueIsAlwaysPresetIfValueForANodeIsMissing() {
        readSourceDocument("/nullvalue/null-value-in-list.yaml");


        String query = "MATCH (:Yaml:Sequence)-[HAS_ITEM]->(item:Yaml:Scalar) " +
                       "WHERE EXISTS(item.value) " +
                       "RETURN item";

        TestResult testResult = query(query);
        assertThat(testResult.getColumns()).containsKeys("item");

        List<YMLScalarDescriptor> result = testResult.getColumn("item");

        assertThat(result).hasSize(3);
    }

    @Test
    void theValueOfAMissingContentForANodeMustBeAnEmptyString() {
        readSourceDocument("/nullvalue/null-value-in-list.yaml");


        String query = "MATCH (:Yaml:Sequence)-[HAS_ITEM]->(item:Yaml:Scalar { value: '' }) " +
                       "RETURN item";

        List<YMLScalarDescriptor> result = query(query).getColumn("item");

        assertThat(result).hasSize(1);
    }


    @Test
    void theParsedFileContainsMultipleEmptyDocuments() {
        File yamlFile = new File(getClassesDirectory(YMLFileScannerPlugin.class),
                                 "/nullvalue/multiple-empty-documents.yaml");

        YMLFileDescriptor result = getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        assertThat(result).isNotNull().hasDocuments(3)
                          .getDocumentByParseIndex(0)
                          .andContinueAssertionOnThis()
                          .hasScalars(1)
                          .getScalarByParseIndex(0)
                          .andContinueAssertionOnThis()
                          .hasEmptyValue();
    }

}
