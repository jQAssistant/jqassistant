package com.buschmais.jqassistant.plugin.java.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.ContainerFileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.ArtifactScopedTypeResolver;
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
     */
    protected void scanClasses(Class<?>... classes) {
        this.scanClasses(ARTIFACT_ID, classes);
    }

    /**
     * Scans the given classes.
     * 
     * @param outerClass
     *            The outer classes.
     * @param innerClassName
     *            The outer classes.
     */
    protected void scanInnerClass(Class<?> outerClass, String innerClassName) throws ClassNotFoundException {
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
     */
    protected void scanClasses(String artifactId, final Class<?>... classes) {
        execute(artifactId, (artifact, scanner) -> {
            List<FileDescriptor> result = new ArrayList<>();
            for (Class<?> item : classes) {
                FileDescriptor fileDescriptor = scanner.scan(item, item.getName(), JavaScope.CLASSPATH);
                result.add(fileDescriptor);
            }
            return result;
        });
    }

    protected void scanClassPathResource(Scope scope, String resource) {
        scanClassPathResources(scope, ARTIFACT_ID, resource);
    }

    protected void scanClassPathResources(final Scope scope, String artifactId, final String... resources) {
        final File directory = getClassesDirectory(this.getClass());
        execute(artifactId, (artifact, scanner) -> {
            List<FileDescriptor> result = new ArrayList<>();
            for (String resource : resources) {
                File file = new File(directory, resource);
                FileDescriptor fileDescriptor = scanner.scan(file, resource, scope);
                result.add(fileDescriptor);
            }
            return result;
        });
    }

    /**
     * Scans the a directory.
     *
     * @param directory
     *            The directory.
     */
    protected void scanClassPathDirectory(File directory) {
        scanClassPathDirectory(ARTIFACT_ID, directory);
    }

    /**
     * Scans the a directory.
     * 
     * @param artifactId
     *            The artifact to use.
     * @param directory
     *            The directory.
     */
    protected void scanClassPathDirectory(String artifactId, final File directory) {
        store.beginTransaction();
        JavaClassesDirectoryDescriptor artifactDescriptor = getArtifactDescriptor(artifactId);
        execute(artifactDescriptor, (artifact, scanner) -> {
            scanner.scan(directory, directory.getAbsolutePath(), JavaScope.CLASSPATH);
            return Collections.emptyList();
        }, getScanner());
        store.commitTransaction();
    }

    /**
     * Executes the given scan operation.
     * 
     * @param artifactId
     *            The artifact id of the artifact to push on the context.
     * @param operation
     *            The operation.
     */
    protected List<? extends FileDescriptor> execute(String artifactId, ScanClassPathOperation operation) {
        Scanner scanner = getScanner();
        ScannerContext context = scanner.getContext();
        store.beginTransaction();
        JavaArtifactFileDescriptor artifact = getArtifactDescriptor(artifactId);
        artifact.setFullQualifiedName(artifactId);
        context.push(JavaArtifactFileDescriptor.class, artifact);
        ContainerFileResolver fileResolverStrategy = new ContainerFileResolver(artifact);
        context.push(FileResolver.class, fileResolverStrategy);

        List<? extends FileDescriptor> descriptors = execute(artifact, operation, scanner);
        for (FileDescriptor descriptor : descriptors) {
            fileResolverStrategy.put(descriptor.getFileName(), descriptor);
        }

        context.pop(JavaArtifactFileDescriptor.class);
        context.pop(FileResolver.class);
        fileResolverStrategy.flush();
        store.commitTransaction();
        return descriptors;
    }

    protected List<? extends FileDescriptor> execute(JavaArtifactFileDescriptor artifact, ScanClassPathOperation operation, Scanner scanner) {
        ScannerContext context = scanner.getContext();
        context.push(JavaArtifactFileDescriptor.class, artifact);
        context.push(TypeResolver.class, new ArtifactScopedTypeResolver(artifact));
        List<? extends FileDescriptor> descriptors = operation.scan(artifact, scanner);
        context.pop(TypeResolver.class);
        context.pop(JavaArtifactFileDescriptor.class);
        return descriptors;
    }

    /**
     * Operation to execute for scanning a classpath.
     */
    protected interface ScanClassPathOperation {

        /**
         * Perform the scan.
         * 
         * @param artifact
         *            The artifact.
         * @param scanner
         *            The scanner.
         */
        List<FileDescriptor> scan(JavaArtifactFileDescriptor artifact, Scanner scanner);
    }
}
