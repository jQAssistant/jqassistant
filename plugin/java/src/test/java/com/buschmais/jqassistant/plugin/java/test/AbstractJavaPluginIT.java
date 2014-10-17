package com.buschmais.jqassistant.plugin.java.test;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

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
        for (Class<?> item : classes) {
            FileDescriptor fileDescriptor = getScanner().scan(item, JavaScope.CLASSPATH);
            artifact.addContains(fileDescriptor);
        }
        store.commitTransaction();
    }

    protected void scanClassPathResource(Scope scope, String resource) throws IOException {
        scanClassPathResources(scope, ARTIFACT_ID, resource);
    }

    protected void scanClassPathResources(Scope scope, String artifactId, String... resources) throws IOException {
        File directory = getClassesDirectory(this.getClass());
        store.beginTransaction();
        ArtifactDescriptor artifact = artifactId != null ? getArtifactDescriptor(artifactId) : null;
        for (String resource : resources) {
            File file = new File(directory, resource);
            FileDescriptor fileDescriptor = getScanner().scan(file, resource, scope);
            artifact.addContains(fileDescriptor);
        }
        store.commitTransaction();
    }

    /**
     * Scans the a directory.
     *
     * @param directory
     *            The directory.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    protected void scanClassPathDirectory(File directory) throws IOException {
        store.beginTransaction();
        ArtifactDirectoryDescriptor artifact = getArtifactDescriptor(ARTIFACT_ID);
        Scanner scanner = getScanner();
        scanner.getContext().push(ArtifactDescriptor.class, artifact);
        scanner.scan(directory, JavaScope.CLASSPATH);
        scanner.getContext().pop(ArtifactDescriptor.class);
        store.commitTransaction();
    }
}
