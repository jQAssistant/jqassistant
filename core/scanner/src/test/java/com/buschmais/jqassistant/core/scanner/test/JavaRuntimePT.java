package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

public class JavaRuntimePT extends AbstractScannerIT {

    /**
     * The list of primitive types.
     */
    public static final Class<?>[] PRIMITIVE_TYPES = new Class<?>[]{void.class, boolean.class, short.class, int.class, float.class, double.class, long.class};

    private ArtifactDescriptor artifactDescriptor;

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
        getArtifactScanner().scanArchive(artifactDescriptor, runtimeJar);
        store.commitTransaction();
        long expectedTypeCount = classScannerPlugin.getScannedClasses() + PRIMITIVE_TYPES.length;
        assertThat(query("MATCH a-[:CONTAINS]->t:TYPE RETURN COUNT(DISTINCT t) as types").getColumn("types"), hasItem(expectedTypeCount));
    }

}
