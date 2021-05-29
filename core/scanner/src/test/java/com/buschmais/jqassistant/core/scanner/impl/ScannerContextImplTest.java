package com.buschmais.jqassistant.core.scanner.impl;

import java.io.File;

import com.buschmais.jqassistant.core.store.api.Store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Verifies the functionality of {@link ScannerContextImpl}.
 */
@ExtendWith(MockitoExtension.class)
public class ScannerContextImplTest {

    public static final File OUTPUT_DIRECTORY = new File(".");

    @Mock
    private Store store;

    private ScannerContextImpl scannerContext;

    @BeforeEach
    public void setUp() {
        scannerContext = new ScannerContextImpl(store, OUTPUT_DIRECTORY);
    }

    @Test
    public void peekNonExistingValue() {

        assertThatThrownBy(() -> scannerContext.peek(String.class)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void peekExistingValues() {
        scannerContext.push(String.class, "Foo");
        assertThat(scannerContext.peek(String.class), equalTo("Foo"));
        scannerContext.push(String.class, "Bar");
        assertThat(scannerContext.peek(String.class), equalTo("Bar"));
        assertThat(scannerContext.pop(String.class), equalTo("Bar"));
        assertThat(scannerContext.peek(String.class), equalTo("Foo"));
        assertThat(scannerContext.pop(String.class), equalTo("Foo"));
        try {
            scannerContext.peek(String.class);
            fail("Expecting an " + IllegalStateException.class.getName());
        } catch (IllegalStateException e) {
            return;
        }
    }

    @Test
    public void peekDefaultValues() {
        assertThat(scannerContext.peekOrDefault(String.class, "Bar"), equalTo("Bar"));
        scannerContext.push(String.class, "Foo");
        assertThat(scannerContext.peekOrDefault(String.class, "Bar"), equalTo("Foo"));
        assertThat(scannerContext.pop(String.class), equalTo("Foo"));
        assertThat(scannerContext.peekOrDefault(String.class, "Bar"), equalTo("Bar"));
    }

    @Test
    public void dataDirectory() {
        File test = scannerContext.getDataDirectory("test");
        assertThat(test.getAbsoluteFile(), equalTo(new File(OUTPUT_DIRECTORY, "data/test").getAbsoluteFile()));
    }
}
