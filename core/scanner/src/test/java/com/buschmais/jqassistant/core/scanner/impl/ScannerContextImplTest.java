package com.buschmais.jqassistant.core.scanner.impl;

import java.io.File;

import com.buschmais.jqassistant.core.store.api.Store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

/**
 * Verifies the functionality of {@link ScannerContextImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ScannerContextImplTest {

    private static final File WORKING_DIRECTORY = new File(".");

    private static final File OUTPUT_DIRECTORY = new File(WORKING_DIRECTORY, "jqassistant");

    @Mock
    private Store store;

    private ScannerContextImpl scannerContext;

    @BeforeEach
    void setUp() {
        scannerContext = new ScannerContextImpl(ScannerContextImplTest.class.getClassLoader(), store, WORKING_DIRECTORY, OUTPUT_DIRECTORY);
    }

    @Test
    void peekNonExistingValue() {

        assertThatThrownBy(() -> scannerContext.peek(String.class)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void peekExistingValues() {
        scannerContext.push(String.class, "Foo");
        assertThat(scannerContext.peek(String.class)).isEqualTo("Foo");
        scannerContext.push(String.class, "Bar");
        assertThat(scannerContext.peek(String.class)).isEqualTo("Bar");
        assertThat(scannerContext.pop(String.class)).isEqualTo("Bar");
        assertThat(scannerContext.peek(String.class)).isEqualTo("Foo");
        assertThat(scannerContext.pop(String.class)).isEqualTo("Foo");
        try {
            scannerContext.peek(String.class);
            fail("Expecting an " + IllegalStateException.class.getName());
        } catch (IllegalStateException e) {
            return;
        }
    }

    @Test
    void peekDefaultValues() {
        assertThat(scannerContext.peekOrDefault(String.class, "Bar")).isEqualTo("Bar");
        scannerContext.push(String.class, "Foo");
        assertThat(scannerContext.peekOrDefault(String.class, "Bar")).isEqualTo("Foo");
        assertThat(scannerContext.pop(String.class)).isEqualTo("Foo");
        assertThat(scannerContext.peekOrDefault(String.class, "Bar")).isEqualTo("Bar");
    }

    @Test
    void dataDirectory() {
        File test = scannerContext.getDataDirectory("test");
        assertThat(test.getAbsoluteFile()).isEqualTo(new File(OUTPUT_DIRECTORY, "data/test").getAbsoluteFile());
    }
}
