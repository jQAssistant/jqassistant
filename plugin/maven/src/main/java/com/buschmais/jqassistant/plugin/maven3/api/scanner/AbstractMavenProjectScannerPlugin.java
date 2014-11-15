package com.buschmais.jqassistant.plugin.maven3.api.scanner;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;

/**
 * Abstract base class for maven project scanner plugins.
 */
public abstract class AbstractMavenProjectScannerPlugin extends AbstractScannerPlugin<MavenProject, MavenProjectDirectoryDescriptor> {

    /**
     * The artifact type for test jars.
     */
    public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

    @Override
    public Class<? extends MavenProject> getType() {
        return MavenProject.class;
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

    protected <A extends ArtifactDescriptor> A resolveArtifact(Artifact artifact, Class<A> descriptorType, ScannerContext scannerContext) {
        boolean testJar = ARTIFACTTYPE_TEST_JAR.equals(artifact.getType());
        return resolveArtifact(artifact, descriptorType, testJar, scannerContext);
    }

    protected <A extends ArtifactDescriptor> A resolveArtifact(Artifact artifact, Class<A> descriptorType, boolean testJar, ScannerContext scannerContext) {
        Store store = scannerContext.getStore();
        String id = createId(artifact, testJar);
        ArtifactDescriptor artifactDescriptor = store.find(ArtifactDescriptor.class, id);
        if (artifactDescriptor == null) {
            artifactDescriptor = store.create(descriptorType, id);
            artifactDescriptor.setFullQualifiedName(id);
            artifactDescriptor.setGroup(artifact.getGroupId());
            artifactDescriptor.setName(artifact.getArtifactId());
            artifactDescriptor.setVersion(artifact.getVersion());
            artifactDescriptor.setClassifier(artifact.getClassifier());
            artifactDescriptor.setType(testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType());
        } else if (!(descriptorType.isAssignableFrom(artifactDescriptor.getClass()))) {
            return store.migrate(artifactDescriptor, descriptorType);
        }
        return descriptorType.cast(artifactDescriptor);
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
