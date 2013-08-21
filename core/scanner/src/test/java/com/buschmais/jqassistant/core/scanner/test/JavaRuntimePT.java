package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.scanner.api.ArtifactScanner;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class JavaRuntimePT extends AbstractScannerIT {

    public class JavaRuntimeScanListener extends ArtifactScanner.ScanListener {

        private int scannedClasses = 0;

        @Override
        public void afterClass() {
            scannedClasses++;
        }

        @Override
        public void beforePackage() {
            store.beginTransaction();
        }

        @Override
        public void afterPackage() {
            store.commitTransaction();
        }
    }

    private JavaRuntimeScanListener scanListener;

    @Before
    public void createListener() {
        scanListener = new JavaRuntimeScanListener();
    }

    @Test
    public void javaRuntime() throws IOException {
        String javaHome = System.getProperty("java.home");
        Assume.assumeNotNull("java.home is not set.", javaHome);
        File runtimeJar = new File(javaHome + "/lib/rt.jar");
        Assume.assumeTrue("Java Runtime JAR not found: " + runtimeJar.getAbsolutePath(), runtimeJar.exists());
        getArtifactScanner().scanArchive(runtimeJar);
    }

    @Override
    protected ArtifactScanner.ScanListener getScanListener() {
        return scanListener;
    }
}
