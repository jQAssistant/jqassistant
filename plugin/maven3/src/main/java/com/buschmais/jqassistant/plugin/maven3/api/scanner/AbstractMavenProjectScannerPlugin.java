package com.buschmais.jqassistant.plugin.maven3.api.scanner;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDescriptor;

/**
 * Abstract base class for maven project scanner plugins.
 */
public abstract class AbstractMavenProjectScannerPlugin extends AbstractScannerPlugin<MavenProject> {

    /**
     * The artifact type for test jars.
     */
    public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

    @Override
    public Class getType() {
        return MavenProject.class;
    }

    @Override
    protected void initialize() {
    }

    protected <T extends MavenProjectDescriptor> T resolveProject(MavenProject project, Class<T> expectedType, ScannerContext scannerContext) {
        Store store = scannerContext.getStore();
        String id = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
        MavenProjectDescriptor projectDescriptor = store.find(MavenProjectDescriptor.class, id);
        if (projectDescriptor == null) {
            projectDescriptor = store.create(expectedType, id);
            projectDescriptor.setName(project.getName());
            projectDescriptor.setGroupId(project.getGroupId());
            projectDescriptor.setArtifactId(project.getArtifactId());
            projectDescriptor.setVersion(project.getVersion());
        } else if (!expectedType.isAssignableFrom(projectDescriptor.getClass())) {
            projectDescriptor = store.migrate(projectDescriptor, expectedType);
        }
        return expectedType.cast(projectDescriptor);
    }

    protected ArtifactDescriptor resolveArtifact(Artifact artifact, ScannerContext scannerContext) {
        boolean testJar = ARTIFACTTYPE_TEST_JAR.equals(artifact.getType());
        return resolveArtifact(artifact, testJar, scannerContext);
    }

    protected ArtifactDescriptor resolveArtifact(Artifact artifact, boolean testJar, ScannerContext scannerContext) {
        Store store = scannerContext.getStore();
        String id = createId(artifact, testJar);
        ArtifactDescriptor artifactDescriptor = store.find(ArtifactDescriptor.class, id);
        if (artifactDescriptor == null) {
            artifactDescriptor = store.create(ArtifactDirectoryDescriptor.class, id);
            artifactDescriptor.setFullQualifiedName(id);
            artifactDescriptor.setGroup(artifact.getGroupId());
            artifactDescriptor.setName(artifact.getArtifactId());
            artifactDescriptor.setVersion(artifact.getVersion());
            artifactDescriptor.setClassifier(artifact.getClassifier());
            artifactDescriptor.setType(testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType());
        }
        return artifactDescriptor;
    }

    /**
     * Creates the id of an artifact descriptor by the given items.
     * 
     * @param artifact
     *            The maven artifact.
     * @param testJar
     *            If a test-jar type shall be used.
     * @return The id.
     */
    private String createId(Artifact artifact, boolean testJar) {
        StringBuffer id = new StringBuffer();
        id.append(artifact.getGroupId());
        id.append(':');
        id.append(artifact.getArtifactId());
        id.append(':');
        id.append(testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType());
        String classifier = artifact.getClassifier();
        if (classifier != null) {
            id.append(':');
            id.append(classifier);
        }
        id.append(':');
        id.append(artifact.getVersion());
        return id.toString();
    }
}
