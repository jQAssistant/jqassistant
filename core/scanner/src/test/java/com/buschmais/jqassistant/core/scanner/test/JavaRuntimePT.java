package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.scanner.api.ArtifactScanner;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class JavaRuntimePT extends AbstractScannerIT {


    /**
     * A specific scan listener which counts the number of scanned classes and wraps a transaction around each package.
     */
    public class JavaRuntimeScanListener extends ArtifactScanner.ScanListener {

        private int scannedClasses = 0;

        @Override
        public void afterClass() {
            scannedClasses++;
        }

        @Override
        public void beforePackage() {
            store.beginTransaction();
            artifactDescriptor = store.find(ArtifactDescriptor.class, "rt");
        }

        @Override
        public void afterPackage() {
            store.commitTransaction();
        }

        public int getScannedClasses() {
            return scannedClasses;
        }
    }

    /**
     * The list of primitive types.
     */
    public static final Class<?>[] PRIMITIVE_TYPES = new Class<?>[]{void.class, boolean.class, short.class, int.class, float.class, double.class, long.class};

    private ArtifactDescriptor artifactDescriptor;

    private JavaRuntimeScanListener scanListener;

    @Before
    public void createListener() {
        scanListener = new JavaRuntimeScanListener();
    }

    /**
     * Scans the rt.jar of the Java Runtime Environment specified by the enviroment variable java.home.
     *
     * @throws IOException If scanning fails.
     */
    @Test
    public void javaRuntime() throws IOException {
        String javaHome = System.getProperty("java.home");
        Assume.assumeNotNull("java.home is not set.", javaHome);
        File runtimeJar = new File(javaHome + "/lib/rt.jar");
        Assume.assumeTrue("Java Runtime JAR not found: " + runtimeJar.getAbsolutePath(), runtimeJar.exists());
        store.beginTransaction();
        artifactDescriptor = store.create(ArtifactDescriptor.class, "rt");
        store.commitTransaction();
        getArtifactScanner().scanArchive(artifactDescriptor, runtimeJar);
        long expectedTypeCount = scanListener.getScannedClasses() + PRIMITIVE_TYPES.length;
        assertThat(executeQuery("MATCH a-[:CONTAINS]->t:TYPE RETURN COUNT(DISTINCT t) as types").getColumns().get("types"), hasItem(expectedTypeCount));
    }

    @Override
    protected ArtifactScanner.ScanListener getScanListener() {
        return scanListener;
    }
}
