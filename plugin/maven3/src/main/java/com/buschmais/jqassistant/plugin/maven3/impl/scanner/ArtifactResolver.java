package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Dependency;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.xo.api.Query;

public class ArtifactResolver {

    interface Coordinates {

        String getGroupId();

        String getArtifactId();

        String getClassifier();

        String getType();

        String getVersion();

        String getId();
    }

    static class ArtifactCoordinates implements Coordinates {

        /**
         * The artifact type for test jars.
         */
        public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

        private org.apache.maven.artifact.Artifact artifact;
        private boolean testJar;

        ArtifactCoordinates(org.apache.maven.artifact.Artifact artifact, boolean testJar) {
            this.artifact = artifact;
            this.testJar = testJar;
        }

        @Override
        public String getGroupId() {
            return artifact.getGroupId();
        }

        @Override
        public String getArtifactId() {
            return artifact.getArtifactId();
        }

        @Override
        public String getClassifier() {
            return artifact.getClassifier();
        }

        @Override
        public String getType() {
            return testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType();
        }

        @Override
        public String getVersion() {
            return artifact.getVersion();
        }

        @Override
        public String getId() {
            return ArtifactResolver.createId(this);
        }
    }

    static class DependencyCoordinates implements Coordinates {

        private Dependency dependency;

        DependencyCoordinates(Dependency dependency) {
            this.dependency = dependency;
        }

        @Override
        public String getGroupId() {
            return dependency.getGroupId();
        }

        @Override
        public String getArtifactId() {
            return dependency.getArtifactId();
        }

        @Override
        public String getClassifier() {
            return dependency.getClassifier();
        }

        @Override
        public String getType() {
            return dependency.getType();
        }

        @Override
        public String getVersion() {
            return dependency.getVersion();
        }

        @Override
        public String getId() {
            return ArtifactResolver.createId(this);
        }
    }

    static <A extends ArtifactDescriptor> A resolve(Coordinates coordinates, Class<A> descriptorType, ScannerContext scannerContext) {
        Store store = scannerContext.getStore();
        String id = coordinates.getId();
        Map<String, Object> params = new HashMap<>();
        params.put("fqn", id);
        Query.Result<Query.Result.CompositeRowObject> result = store.executeQuery("MATCH (a:Artifact) WHERE a.fqn={fqn} RETURN a", params);
        ArtifactDescriptor artifactDescriptor;
        if (!result.hasResult()) {
            artifactDescriptor = createArtifactDescriptor(coordinates, descriptorType, scannerContext);
        } else {
            artifactDescriptor = result.getSingleResult().get("a", ArtifactDescriptor.class);
            if (!(descriptorType.isAssignableFrom(artifactDescriptor.getClass()))) {
                return store.migrate(artifactDescriptor, descriptorType);
            }
        }
        return descriptorType.cast(artifactDescriptor);
    }

    private static <A extends ArtifactDescriptor> A createArtifactDescriptor(Coordinates coordinates, Class<A> descriptorType, ScannerContext scannerContext) {
        String id = coordinates.getId();
        A artifactDescriptor = scannerContext.getStore().create(descriptorType, id);
        artifactDescriptor.setGroup(coordinates.getGroupId());
        artifactDescriptor.setName(coordinates.getArtifactId());
        if (coordinates.getVersion() == null) {
            artifactDescriptor.setVersion(coordinates.getVersion());
        }
        artifactDescriptor.setVersion(coordinates.getVersion());
        artifactDescriptor.setClassifier(coordinates.getClassifier());
        artifactDescriptor.setType(coordinates.getType());
        return artifactDescriptor;
    }

    /**
     * Creates the id of an coordinates descriptor by the given items.
     *
     * @param coordinates
     *            The maven coordinates.
     * @return The id.
     */
    private static String createId(Coordinates coordinates) {
        StringBuffer id = new StringBuffer();
        if (coordinates.getGroupId() != null) {
            id.append(coordinates.getGroupId());
        }
        id.append(':');
        id.append(coordinates.getArtifactId());
        id.append(':');
        id.append(coordinates.getType());
        String classifier = coordinates.getClassifier();
        if (classifier != null) {
            id.append(':');
            id.append(classifier);
        }
        id.append(':');
        if (coordinates.getVersion() != null) {
            id.append(coordinates.getVersion());
        }
        return id.toString();
    }
}
