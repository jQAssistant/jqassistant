package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import java.io.File;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLScalarDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

class UTF8StringsIT extends AbstractPluginIT {

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        store.commitTransaction();
    }

    @ValueSource(strings = {"\ud83d\ude2e", "\u65e5", "\u6708"})
    @ParameterizedTest
    void documentWithNonLatingStrings(String expectedLiteral) {
        // Used literals
        // Simley:     \u6708
        // Kanji Moon: \u65e5
        // Kanji Sun:  \ud83d\ude2e

        File yamlFile = new File(getClassesDirectory(YMLFileScannerPlugin.class),
                                 "/utf8/document-with-non-ascii-scalars.yaml");
        getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);

        String query = format("MATCH (:Yaml:Sequence)-[HAS_ITEM]" +
                                     "->(item:Yaml:Scalar { value: '%s' }) " +
                                     "RETURN item", expectedLiteral);

        List<YMLScalarDescriptor> result = query(query).getColumn("item");

        assertThat(result).hasSize(1);
    }
}
