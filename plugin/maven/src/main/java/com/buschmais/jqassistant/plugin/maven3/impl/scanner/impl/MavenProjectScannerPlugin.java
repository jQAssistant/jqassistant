package com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl;

import static com.buschmais.jqassistant.core.scanner.api.iterable.IterableConsumer.Consumer;
import static com.buschmais.jqassistant.core.scanner.api.iterable.IterableConsumer.consume;
import static com.buschmais.jqassistant.plugin.java.api.JavaScope.CLASSPATH;
import static com.buschmais.jqassistant.plugin.junit4.api.JunitScope.TESTREPORTS;
import static java.util.Collections.emptyList;

import java.io.File;
import java.io.IOException;

import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.api.AbstractMavenProjectScannerPlugin;

/**
 * A project scanner plugin for maven projects.
 */
public class MavenProjectScannerPlugin extends AbstractMavenProjectScannerPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProjectScannerPlugin.class);

    @Override
    public boolean accepts(MavenProject item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public Iterable<FileDescriptor> scan(MavenProject project, String path, Scope scope, Scanner scanner) throws IOException {
        scanDirectory(project, scanner, project.getBuild().getOutputDirectory(), false);
        scanDirectory(project, scanner, project.getBuild().getTestOutputDirectory(), true);
        scanTestReports(scanner, project.getBuild().getDirectory() + "/surefire-reports");
        scanTestReports(scanner, project.getBuild().getDirectory() + "/failsafe-reports");
        return emptyList();
    }

    /**
     * Scan the given directory for classes.
     * 
     * @param directoryName
     *            The directory.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    private void scanDirectory(MavenProject project, Scanner scanner, final String directoryName, boolean testJar) throws IOException {
        final File directory = new File(directoryName);
        if (!directory.exists()) {
            LOGGER.info("Directory '" + directory.getAbsolutePath() + "' does not exist, skipping scan.");
        } else {
            Store store = getStore();
            store.beginTransaction();
            try {
                final ArtifactDescriptor artifactDescriptor = getArtifact(project, testJar);
                consume(scanner.scan(directory, CLASSPATH), new Consumer<FileDescriptor>() {
                    @Override
                    public void next(FileDescriptor fileDescriptor) {
                        artifactDescriptor.addContains(fileDescriptor);
                    }
                });
            } finally {
                store.commitTransaction();
            }
        }
    }

    /**
     * Scans a directory for test reports.
     * 
     * @param directoryName
     *            The directory name.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    private void scanTestReports(Scanner scanner, String directoryName) throws IOException {
        final File directory = new File(directoryName);
        Store store = getStore();
        if (directory.exists()) {
            store.beginTransaction();
            try {
                consume(scanner.scan(directory, TESTREPORTS), new Consumer<FileDescriptor>() {
                    @Override
                    public void next(FileDescriptor fileDescriptor) {
                    }
                });
            } finally {
                store.commitTransaction();
            }
        }
    }
}
