package com.buschmais.jqassistant.plugin.java.test;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.ClasspathScopedTypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;

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
    protected void scanClasses(String artifactId, final Class<?>... classes) throws IOException {
        execute(artifactId, new ScanClassPathOperation() {
            @Override
            public void scan(JavaArtifactFileDescriptor artifact, Scanner scanner) {
                for (Class<?> item : classes) {
                    FileDescriptor fileDescriptor = scanner.scan(item, item.getName(), JavaScope.CLASSPATH);
                    artifact.getContains().add(fileDescriptor);
                }
            }
        });
    }

    protected void scanClassPathResource(Scope scope, String resource) throws IOException {
        scanClassPathResources(scope, ARTIFACT_ID, resource);
    }

    protected void scanClassPathResources(final Scope scope, String artifactId, final String... resources) throws IOException {
        final File directory = getClassesDirectory(this.getClass());
        execute(artifactId, new ScanClassPathOperation() {
            @Override
            public void scan(JavaArtifactFileDescriptor artifact, Scanner scanner) {
                for (String resource : resources) {
                    File file = new File(directory, resource);
                    FileDescriptor fileDescriptor = scanner.scan(file, resource, scope);
                    artifact.getContains().add(fileDescriptor);
                }
            }
        });
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
    protected void scanClassPathDirectory(String artifactId, final File directory) throws IOException {
        execute(artifactId, new ScanClassPathOperation() {
            @Override
            public void scan(JavaArtifactFileDescriptor artifact, Scanner scanner) {
                scanner.scan(directory, directory.getAbsolutePath(), JavaScope.CLASSPATH);
            }
        });
    }

    protected void execute(String artifactId, ScanClassPathOperation operation) {
        store.beginTransaction();
        JavaArtifactFileDescriptor artifact = getArtifactDescriptor(artifactId);
        Scanner scanner = getScanner();
        scanner.getContext().push(JavaArtifactFileDescriptor.class, artifact);
        scanner.getContext().push(TypeResolver.class, new ClasspathScopedTypeResolver(artifact));
        operation.scan(artifact, scanner);
        scanner.getContext().pop(TypeResolver.class);
        scanner.getContext().pop(JavaArtifactFileDescriptor.class);
        artifact.setFullQualifiedName(artifactId);
        store.commitTransaction();
    }

    protected interface ScanClassPathOperation {
        void scan(JavaArtifactFileDescriptor artifact, Scanner scanner);
    }
}
