package com.buschmais.jqassistant.scm.cli.test;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.scm.cli.Main;

/**
 * Verifies command line scanning.
 */
public class ScanTest {

    @Test
    public void directories() throws IOException {
        String[] args = new String[] { "scan", "-d", "target/classes,target/test-classes" };
        Main.main(args);
    }
}
