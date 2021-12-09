package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.Test;

import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

class JavaRuntimePT extends AbstractJavaPluginIT {

    /**
     * The list of primitive types.
     */
    public static final Class<?>[] PRIMITIVE_TYPES = new Class<?>[] { void.class, boolean.class, short.class, int.class, float.class, double.class,
        long.class };

    /**
     * Scans the rt.jar of the Java Runtime Environment specified by the environment
     * variable java.home.
     */
    @Test
    @TestStore(type = TestStore.Type.FILE)
    void javaRuntime01Scan() {
        String javaHome = System.getProperty("java.home");
        assumeNotNull("java.home is not set.", javaHome);
        File runtimeJar = new File(javaHome + "/lib/rt.jar");
        assumeTrue("Java Runtime JAR not found: " + runtimeJar.getAbsolutePath(), runtimeJar.exists());
        store.beginTransaction();
        getScanner().scan(runtimeJar, runtimeJar.getAbsolutePath(), null);
        store.commitTransaction();
    }

    @Test
    @TestStore(type = TestStore.Type.FILE, reset = false)
    public void javaRuntime02VirtualDependsOn() throws RuleException {
        applyConcept("java:VirtualDependsOn");
    }

    @Test
    @TestStore(type = TestStore.Type.FILE, reset = false)
    public void javaRuntime03VirtualInvokes() throws RuleException {
        applyConcept("java:VirtualInvokes");
    }
}
