package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.io.IOException;

import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;

/**
 * A project scanner plugin for maven projects.
 */
public class MavenProjectScannerPlugin extends AbstractMavenProjectScannerPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProjectScannerPlugin.class);

    @Override
    public void scan(FileScanner fileScanner) throws IOException {
        MavenProject project = getProject();
        scanDirectory(fileScanner, project.getBuild().getOutputDirectory(), false);
        scanDirectory(fileScanner, project.getBuild().getTestOutputDirectory(), true);
        scanTestReports(fileScanner, project.getBuild().getDirectory() + "/surefire-reports");
        scanTestReports(fileScanner, project.getBuild().getDirectory() + "/failsafe-reports");
    }

    /**
     * Scan the given directory for classes.
     * 
     * @param directoryName
     *            The directory.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    private void scanDirectory(FileScanner fileScanner, final String directoryName, boolean testJar) throws IOException {
        final File directory = new File(directoryName);
        if (!directory.exists()) {
            LOGGER.info("Directory '" + directory.getAbsolutePath() + "' does not exist, skipping scan.");
        } else {
            Store store = getStore();
            store.beginTransaction();
            try {
                ArtifactDescriptor artifactDescriptor = getArtifact(testJar);
                for (FileDescriptor descriptor : fileScanner.scanDirectory(directory)) {
                    artifactDescriptor.addContains(descriptor);
                }
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
    private void scanTestReports(FileScanner fileScanner, String directoryName) throws IOException {
        final File directory = new File(directoryName);
        Store store = getStore();
        if (directory.exists()) {
            store.beginTransaction();
            ArtifactDescriptor artifact = getArtifact(true);
            try {
                for (FileDescriptor descriptor : fileScanner.scanDirectory(directory, false)) {
                    artifact.getContains().add(descriptor);
                }
            } finally {
                store.commitTransaction();
            }
        }
    }
}
