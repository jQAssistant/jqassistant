package com.buschmais.jqassistant.plugin.java.test;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

public abstract class AbstractJavaPluginIT extends AbstractPluginIT {

    /**
     * Get or create an
     * {@link com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor}
     * .
     *
     * @param artifactId
     *            The artifact id.
     * @return The
     *         {@link com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor}
     *         .
     */
    protected JavaClassesDirectoryDescriptor getArtifactDescriptor(String artifactId) {
        ArtifactDescriptor artifact = store.find(ArtifactDescriptor.class, artifactId);
        if (artifact == null) {
            artifact = store.create(JavaClassesDirectoryDescriptor.class, artifactId);
            artifact.setFullQualifiedName(artifactId);
        }
        return JavaClassesDirectoryDescriptor.class.cast(artifact);
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
        JavaArtifactDescriptor artifact = getArtifactDescriptor(artifactId);
        Scanner scanner = getScanner();
        ScannerContext context = scanner.getContext();
        context.push(JavaArtifactDescriptor.class, artifact);
        for (Class<?> item : classes) {
            FileDescriptor fileDescriptor = scanner.scan(item, item.getName(), JavaScope.CLASSPATH);
            artifact.getContains().add(fileDescriptor);
        }
        context.pop(JavaArtifactDescriptor.class);
        store.commitTransaction();
    }

    protected void scanClassPathResource(Scope scope, String resource) throws IOException {
        scanClassPathResources(scope, ARTIFACT_ID, resource);
    }

    protected void scanClassPathResources(Scope scope, String artifactId, String... resources) throws IOException {
        File directory = getClassesDirectory(this.getClass());
        store.beginTransaction();
        JavaArtifactDescriptor artifact = artifactId != null ? getArtifactDescriptor(artifactId) : null;
        Scanner scanner = getScanner();
        for (String resource : resources) {
            File file = new File(directory, resource);
            FileDescriptor fileDescriptor = scanner.scan(file, resource, scope);
            artifact.getContains().add(fileDescriptor);
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
        scanClassPathDirectory(ARTIFACT_ID, directory);
    }

    /**
     * Scans the a directory.
     * 
     * @param artifactId
     *            The artifact to use.
     * @param directory
     *            The directory.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    protected void scanClassPathDirectory(String artifactId, File directory) throws IOException {
        store.beginTransaction();
        Scanner scanner = getScanner();
        JavaClassesDirectoryDescriptor scan = scanner.scan(directory, directory.getAbsolutePath(), JavaScope.CLASSPATH);
        scan.setFullQualifiedName(artifactId);
        store.commitTransaction();
    }
}
