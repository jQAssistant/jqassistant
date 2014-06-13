package com.buschmais.jqassistant.plugin.maven3.impl.scanner.api;

import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl.store.MavenProjectDescriptor;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.impl.store.MavenProjectDirectoryDescriptor;

/**
 * Abstract base class for maven project scanner plugins.
 */
public abstract class AbstractMavenProjectScannerPlugin implements ScannerPlugin<MavenProject> {

    /**
     * The artifact type for test jars.
     */
    public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

    private Store store;

    @Override
    public Class getType() {
        return MavenProject.class;
    }

    @Override
    public void initialize(Store store, Map<String, Object> properties) {
        this.store = store;
    }

    protected Store getStore() {
        return store;
    }

    protected MavenProjectDescriptor resolveProject(MavenProject project) {
        MavenProjectDirectoryDescriptor projectDescriptor = resolveProject(project.getArtifact(), MavenProjectDirectoryDescriptor.class);
        projectDescriptor.setFileName(project.getFile().getAbsolutePath());
        projectDescriptor.setPackaging(project.getPackaging());
        return projectDescriptor;
    }

    protected MavenProjectDescriptor resolveProject(Artifact artifact) {
        return resolveProject(artifact, MavenProjectDescriptor.class);
    }

    protected <T extends MavenProjectDescriptor> T resolveProject(Artifact artifact, Class<T> expectedType) {
        String id = createId(artifact.getGroupId(), artifact.getArtifactId(), null, null, artifact.getVersion());
        MavenProjectDescriptor moduleDescriptor = getStore().find(MavenProjectDescriptor.class, id);
        if (moduleDescriptor == null) {
            moduleDescriptor = store.create(expectedType, id);
            moduleDescriptor.setGroupId(artifact.getGroupId());
            moduleDescriptor.setArtifactId(artifact.getArtifactId());
            moduleDescriptor.setVersion(artifact.getVersion());
        } else if (!expectedType.isAssignableFrom(moduleDescriptor.getClass())) {
            moduleDescriptor = getStore().migrate(moduleDescriptor, expectedType);
        }
        return expectedType.cast(moduleDescriptor);
    }

    protected ArtifactDescriptor getArtifact(Artifact artifact, boolean testJar) {
        String type = testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType();
        String id = createId(artifact.getGroupId(), artifact.getArtifactId(), type, artifact.getClassifier(), artifact.getVersion());
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
