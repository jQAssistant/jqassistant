package com.buschmais.jqassistant.plugin.yaml2.impl.scanner;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12.AbstractYAMLPluginIT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ValidityOfYAMLDocumentsIT extends AbstractYAMLPluginIT {

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        store.commitTransaction();
    }

    @Test
    void invalidFilesAreMarkedAsInvalid() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument("/validity/erroneus.yaml");

        assertThat(ymlFileDescriptor.isValid()).isFalse();
    }

    @Test
    void validFilesAreMarkedAsValid() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument("/validity/valid.yml");

        assertThat(ymlFileDescriptor.isValid()).isTrue();
    }
}
