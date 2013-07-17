package com.buschmais.jqassistant.scanner.test;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class JavaRuntimeIT extends AbstractScannerIT {

    @Test
    @Ignore
    public void javaRuntime() throws IOException {
        String javaHome = System.getProperty("java.home");
        Assume.assumeNotNull("java.home is not set.", javaHome);
        File runtimeJar = new File(javaHome + "/lib/rt.jar");
        Assume.assumeTrue("Java Runtime JAR not found: " + runtimeJar.getAbsolutePath(), runtimeJar.exists());
        store.beginTransaction();
        scanner.scanArchive(runtimeJar);
        store.endTransaction();
    }

}
