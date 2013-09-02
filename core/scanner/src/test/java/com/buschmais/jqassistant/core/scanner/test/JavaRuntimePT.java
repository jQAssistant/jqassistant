package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

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
        Iterable<Descriptor> iterable = getArtifactScanner().scanArchive(runtimeJar);
        Iterator<Descriptor> iterator = iterable.iterator();
        Descriptor descriptor;
        do {
            int count = 0;
            store.beginTransaction();
            do {
                if (iterator.hasNext()) {
                    descriptor = iterator.next();
                    count++;
                } else {
                    descriptor = null;
                }
            } while (descriptor != null && count < 10);
            store.commitTransaction();
        } while (descriptor != null);
        long expectedTypeCount = classScannerPlugin.getScannedClasses() + PRIMITIVE_TYPES.length;
        assertThat(query("MATCH a-[:CONTAINS]->t:TYPE RETURN COUNT(DISTINCT t) as types").getColumn("types"), hasItem(expectedTypeCount));
    }

}
