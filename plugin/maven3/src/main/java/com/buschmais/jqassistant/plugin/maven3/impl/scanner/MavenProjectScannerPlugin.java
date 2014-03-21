package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.scanner.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * A project scanner plugin for maven projects.
 */
public class MavenProjectScannerPlugin implements ProjectScannerPlugin {

    /**
     * The artifact type for test jars.
     */
    public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenProjectScannerPlugin.class);

    private Store store;

    private MavenProject project;

    @Override
    public void initialize(Store store, Properties properties) {
        this.store = store;
        this.project = (MavenProject) properties.get(MavenProject.class.getName());
    }

    @Override
    public void scan(FileScanner fileScanner) throws IOException {
        scanDirectory(fileScanner, project.getBuild().getOutputDirectory(), false);
        scanDirectory(fileScanner, project.getBuild().getTestOutputDirectory(), true);
        scanTestReports(fileScanner, project.getBuild().getDirectory() + "/surefire-reports");
        scanTestReports(fileScanner, project.getBuild().getDirectory() + "/failsafe-reports");
    }

    /**
     * Scan the given directory for classes.
     *
     * @param directoryName The directory.
     * @throws java.io.IOException If scanning fails.
     */
    private void scanDirectory(FileScanner fileScanner, final String directoryName, boolean testJar) throws IOException {
        final File directory = new File(directoryName);
        if (!directory.exists()) {
            LOGGER.info("Directory '" + directory.getAbsolutePath() + "' does not exist, skipping scan.");
        } else {
            store.beginTransaction();
            try {
                ArtifactDescriptor artifactDescriptor = getArtifact(project, store, testJar);
                for (FileDescriptor descriptor : fileScanner.scanDirectory(directory)) {
                    artifactDescriptor.getContains().add(descriptor);
                }
            } finally {
                store.commitTransaction();
            }
        }
    }

    /**
     * Scans a directory for test reports.
     *
     * @param directoryName The directory name.
     * @throws java.io.IOException If scanning fails.
     */
    private void scanTestReports(FileScanner fileScanner, String directoryName) throws IOException {
        final File directory = new File(directoryName);
        if (directory.exists()) {
            store.beginTransaction();
            try {
                for (Descriptor descriptor : fileScanner.scanDirectory(directory, false)) {
                }
            } finally {
                store.commitTransaction();
            }
        }
    }

    private ArtifactDescriptor getArtifact(final MavenProject project, Store store, boolean testJar) {
        Artifact artifact = project.getArtifact();
        String type = testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType();
        String id = createArtifactDescriptorId(artifact.getGroupId(), artifact.getArtifactId(), type, artifact.getClassifier(),
                artifact.getVersion());
        ArtifactDescriptor artifactDescriptor = store.find(ArtifactDescriptor.class, id);
        if (artifactDescriptor == null) {
            artifactDescriptor = store.create(ArtifactDescriptor.class, id);
            artifactDescriptor.setGroup(artifact.getGroupId());
            artifactDescriptor.setName(artifact.getArtifactId());
            artifactDescriptor.setVersion(artifact.getVersion());
            artifactDescriptor.setClassifier(artifact.getClassifier());
            artifactDescriptor.setType(type);
        }
        return artifactDescriptor;
    }


    /**
     * Creates the id of an artifact descriptor by the given items.
     *
     * @param group      The group.
     * @param name       The name.
     * @param type       The type.
     * @param classifier The classifier (optional).
     * @param version    The version.
     * @return The id.
     */
    private String createArtifactDescriptorId(String group, String name, String type, String classifier, String version) {
        StringBuffer id = new StringBuffer();
        id.append(group);
        id.append(':');
        id.append(name);
        id.append(':');
        id.append(type);
        if (classifier != null) {
            id.append(':');
            id.append(classifier);
        }
        id.append(':');
        id.append(version);
        return id.toString();
    }
}
