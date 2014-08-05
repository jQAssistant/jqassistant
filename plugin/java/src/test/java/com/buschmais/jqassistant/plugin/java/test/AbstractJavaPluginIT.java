package com.buschmais.jqassistant.plugin.java.test;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.api.scanner.ClassesDirectory;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

public abstract class AbstractJavaPluginIT extends AbstractPluginIT {

    /**
     * Scans the a directory.
     * 
     * @param directory
     *            The directory.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    protected void scanDirectory(Scope scope, File directory) throws IOException {
        store.beginTransaction();
        ArtifactDirectoryDescriptor artifact = getArtifactDescriptor(ARTIFACT_ID);
        getScanner().scan(new ClassesDirectory(directory, artifact), scope);
        store.commitTransaction();
    }

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

    protected void scanResource(Scope scope, String resource) throws IOException {
        scanResources(scope, ARTIFACT_ID, resource);
    }

    protected void scanResources(Scope scope, String artifactId, String... resources) throws IOException {
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

}
