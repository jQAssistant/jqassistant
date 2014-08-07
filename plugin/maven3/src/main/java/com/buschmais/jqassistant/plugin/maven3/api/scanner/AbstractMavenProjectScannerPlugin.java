package com.buschmais.jqassistant.plugin.maven3.api.scanner;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDescriptor;
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

    protected <T extends MavenProjectDescriptor> T resolveProject(MavenProject project, Class<T> expectedType) {
        Artifact artifact = project.getArtifact();
        String id = createId(artifact.getGroupId(), artifact.getArtifactId(), null, null, artifact.getVersion());
        MavenProjectDescriptor projectDescriptor = getStore().find(MavenProjectDescriptor.class, id);
        if (projectDescriptor == null) {
            projectDescriptor = getStore().create(expectedType, id);
            projectDescriptor.setName(project.getName());
            projectDescriptor.setGroupId(artifact.getGroupId());
            projectDescriptor.setArtifactId(artifact.getArtifactId());
            projectDescriptor.setVersion(artifact.getVersion());
        } else if (!expectedType.isAssignableFrom(projectDescriptor.getClass())) {
            projectDescriptor = getStore().migrate(projectDescriptor, expectedType);
        }
        return expectedType.cast(projectDescriptor);
    }

    protected ArtifactDescriptor resolveArtifact(Artifact artifact) {
        boolean testJar = ARTIFACTTYPE_TEST_JAR.equals(artifact.getType());
        return resolveArtifact(artifact, testJar, ArtifactDescriptor.class);
    }

    protected <T extends ArtifactDescriptor> T resolveArtifact(Artifact artifact, boolean testJar, Class<T> expectedType) {
        String type = testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType();
        String id = createId(artifact.getGroupId(), artifact.getArtifactId(), type, artifact.getClassifier(), artifact.getVersion());
        ArtifactDescriptor artifactDescriptor = getStore().find(ArtifactDescriptor.class, id);
        if (artifactDescriptor == null) {
            artifactDescriptor = getStore().create(expectedType, id);
           artifactDescriptor.setGroup(artifact.getGroupId());
            artifactDescriptor.setName(artifact.getArtifactId());
            artifactDescriptor.setVersion(artifact.getVersion());
            artifactDescriptor.setClassifier(artifact.getClassifier());
            artifactDescriptor.setType(type);
        } else if (!expectedType.isAssignableFrom(artifactDescriptor.getClass())) {
            artifactDescriptor = getStore().migrate(artifactDescriptor, expectedType);
        }
        return expectedType.cast(artifactDescriptor);
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
    private String createId(String group, String name, String type, String classifier, String version) {
        StringBuffer id = new StringBuffer();
        id.append(group);
        id.append(':');
        id.append(name);
        if (type != null) {
            id.append(':');
            id.append(type);
        }
        if (classifier != null) {
            id.append(':');
            id.append(classifier);
        }
        id.append(':');
        id.append(version);
        return id.toString();
    }
}
