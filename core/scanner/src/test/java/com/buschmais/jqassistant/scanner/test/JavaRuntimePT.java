package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.ClassScanner;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class JavaRuntimePT extends AbstractScannerIT {

    @Test
    public void javaRuntime() throws IOException {
        String javaHome = System.getProperty("java.home");
        Assume.assumeNotNull("java.home is not set.", javaHome);
        File runtimeJar = new File(javaHome + "/lib/rt.jar");
        Assume.assumeTrue("Java Runtime JAR not found: " + runtimeJar.getAbsolutePath(), runtimeJar.exists());
        scanner.scanArchive(runtimeJar);
    }

    @Override
    protected ClassScanner.ScanListener getScanListener() {
        return new ClassScanner.ScanListener() {
            @Override
            public void beforePackage() {
                store.beginTransaction();
            }

            @Override
            public void afterPackage() {
                store.endTransaction();
            }
        };
    }
}
