package com.buschmais.jqassistant.plugin.java.test.scanner;

import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JavaRuntimePT extends AbstractJavaPluginIT {

    /**
     * The list of primitive types.
     */
    public static final Class<?>[] PRIMITIVE_TYPES = new Class<?>[] { void.class, boolean.class, short.class, int.class, float.class, double.class, long.class };
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaRuntimePT.class);

    /**
     * Scans the rt.jar of the Java Runtime Environment specified by the
     * environment variable java.home.
     *
     * @throws IOException
     *             If scanning fails.
     */
    @Test
    public void javaRuntime01Scan() throws Exception {
        String javaHome = System.getProperty("java.home");
        assumeNotNull("java.home is not set.", javaHome);
        File runtimeJar = new File(javaHome + "/lib/rt.jar");
        assumeTrue("Java Runtime JAR not found: " + runtimeJar.getAbsolutePath(), runtimeJar.exists());
        store.beginTransaction();
        getScanner().scan(runtimeJar, runtimeJar.getAbsolutePath(), null);
        store.commitTransaction();
        applyConcept("javaruntime:PackageDependency");
    }

}
