package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;

/**
 * Abstract base class for maven project scanner plugins.
 */
public abstract class AbstractMavenProjectScannerPlugin implements ProjectScannerPlugin {

    /**
     * The artifact type for test jars.
     */
    public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

    private Store store;
    private MavenProject project;

    @Override
    public void initialize(Store store, Map<String, Object> properties) {
        this.store = store;
        this.project = (MavenProject) properties.get(MavenProject.class.getName());
    }

    protected Store getStore() {
        return store;
    }

    protected MavenProject getProject() {
        return project;
    }

    protected ArtifactDescriptor getArtifact(boolean testJar) {
        Artifact artifact = project.getArtifact();
        String type = testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType();
        String id = createArtifactDescriptorId(artifact.getGroupId(), artifact.getArtifactId(), type, artifact.getClassifier(), artifact.getVersion());
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
     * @param group
     *            The group.
     * @param name
     *            The name.
     * @param type
     *            The type.
     * @param classifier
     *            The classifier (optional).
     * @param version
     *            The version.
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
