package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.io.File;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.YMLFileScannerPlugin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleC2E01IT extends AbstractPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e01-sequence-of-scalars.yaml";

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        store.commitTransaction();
    }

    @Test
    void scannerCanReadDocument() {

        // example-c2-e01-sequence-of-scalars.yaml

        File yamlFile = new File(getClassesDirectory(YMLFileScannerPlugin.class), YAML_FILE);

        YMLFileDescriptor result = getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        assertThat(result).isNotNull();
        assertThat(result.getDocuments()).isNotNull().isNotEmpty();
        assertThat(result.getDocuments()).hasSize(1);

        // todo finish this test!

//        throw new RuntimeException("Please implement me!");
    }
}
