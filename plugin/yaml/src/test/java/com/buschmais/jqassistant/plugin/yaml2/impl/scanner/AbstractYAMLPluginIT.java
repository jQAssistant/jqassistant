package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractYAMLPluginIT extends AbstractPluginIT {
    protected String getSourceYAMLFile() {
        throw new UnsupportedOperationException("Operation noch supported");
    }

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        store.commitTransaction();
    }

    protected YMLFileDescriptor readSourceDocument(String sourceFile) {
        File yamlFile = new File(getClassesDirectory(YMLFileScannerPlugin.class), sourceFile);

        return getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);
    }

    protected YMLFileDescriptor readSourceDocument() {
        String sourceFile = getSourceYAMLFile();
        return readSourceDocument(sourceFile);
    }
}
