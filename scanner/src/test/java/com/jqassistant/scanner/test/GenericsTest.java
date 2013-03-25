package com.jqassistant.scanner.test;

import java.io.IOException;

import org.junit.Test;

import com.jqassistant.scanner.test.sets.generics.GenericType;

public class GenericsTest extends AbstractScannerTest {

    @Test
    public void genericType() throws IOException {
        scanner.scanClass(GenericType.class);
    }

}
