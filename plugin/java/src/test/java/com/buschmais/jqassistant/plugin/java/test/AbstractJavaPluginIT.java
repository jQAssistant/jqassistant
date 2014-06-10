package com.buschmais.jqassistant.plugin.java.test;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.api.JavaScope;

public abstract class AbstractJavaPluginIT extends AbstractPluginIT {

    /**
     * Scans the given classes.
     * 
     * @param classes
     *            The classes.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    protected void scanClasses(Class<?>... classes) throws IOException {
        this.scanClasses(ARTIFACT_ID, classes);
    }

    /**
     * Scans the given classes.
     * 
     * @param outerClass
     *            The outer classes.
     * @param innerClassName
     *            The outer classes.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    protected void scanInnerClass(Class<?> outerClass, String innerClassName) throws IOException, ClassNotFoundException {
        Class<?> innerClass = getInnerClass(outerClass, innerClassName);
        scanClasses(innerClass);
    }

    /**
     * Loads an inner class.
     * 
     * @param outerClass
     *            The out class.
     * @param innerClassName
     *            The name of the inner class.
     * @return The inner class.
     * @throws ClassNotFoundException
     *             If the class cannot be loaded.
     */
    protected Class<?> getInnerClass(Class<?> outerClass, String innerClassName) throws ClassNotFoundException {
        String className = outerClass.getName() + "$" + innerClassName;
        return outerClass.getClassLoader().loadClass(className);
    }

    /**
     * Scans the given classes.
     * 
     * @param artifactId
     *            The id of the containing artifact.
     * @param classes
     *            The classes.
     * @throws IOException
     *             If scanning fails.
     */
    protected void scanClasses(String artifactId, Class<?>... classes) throws IOException {
        store.beginTransaction();
        ArtifactDescriptor artifact = getArtifactDescriptor(artifactId);
        for (FileDescriptor descriptor : getScanner().scan(asList(classes), JavaScope.CLASSPATH)) {
            artifact.addContains(descriptor);
        }
        store.commitTransaction();
    }

    protected void scanResource(Scope scope, String resource) throws IOException {
        scanResources(scope, ARTIFACT_ID, resource);
    }

    protected void scanResources(Scope scope, String artifactId, String... resources) throws IOException {
        File directory = getClassesDirectory(this.getClass());
        store.beginTransaction();
        ArtifactDescriptor artifact = artifactId != null ? getArtifactDescriptor(artifactId) : null;
        for (String resource : resources) {
            File file = new File(directory, resource);
            for (FileDescriptor descriptor : getScanner().scan(file, resource, scope)) {
                artifact.addContains(descriptor);
            }
        }
        store.commitTransaction();
    }

}
