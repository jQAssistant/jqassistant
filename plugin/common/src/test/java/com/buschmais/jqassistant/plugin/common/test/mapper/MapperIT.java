package com.buschmais.jqassistant.plugin.common.test.mapper;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.scanner.api.DefaultScope.NONE;
import static org.assertj.core.api.Assertions.assertThat;

class MapperIT extends AbstractPluginIT {

    @BeforeEach
    void beginTx() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTx() {
        store.commitTransaction();
    }

    @Test
    void mapper() {
        verify(getScanner());
    }

    @Test
    void enricher() {
        Scanner scanner = getScanner();
        FileDescriptor fileDescriptor = scanner.getContext()
            .getStore()
            .create(FileDescriptor.class);
        scanner.getContext()
            .setCurrentDescriptor(fileDescriptor);

        Descriptor descriptor = verify(scanner);
        assertThat(descriptor).isInstanceOfSatisfying(FileDescriptor.class, d -> assertThat((Long) d.getId()).isEqualTo(fileDescriptor.getId()));
    }

    private Descriptor verify(Scanner scanner) {
        Model model = Model.builder()
            .name("test")
            .build();

        Descriptor descriptor = scanner.scan(model, "/", NONE);

        assertThat(descriptor).isInstanceOfSatisfying(ModelDescriptor.class, modelDescriptor -> assertThat(modelDescriptor.getName()).isEqualTo("test"));
        return descriptor;
    }
}
