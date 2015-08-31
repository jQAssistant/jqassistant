package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.MAVEN;
import org.apache.maven.model.Model;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.RepositoryArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.PomModelBuilder;

/**
 * A plugin for (remote) maven artifacts.
 * 
 * @author pherklotz
 */
public class MavenArtifactScannerPlugin extends AbstractScannerPlugin<ArtifactInfo, RepositoryArtifactDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenArtifactScannerPlugin.class);
    private static final String PROPERTY_NAME_ARTIFACTS_KEEP = "m2repo.artifacts.keep";
    private static final String PROPERTY_NAME_ARTIFACTS_SCAN = "m2repo.artifacts.scan";
    private static final String PROPERTY_NAME_FILTER_INCLUDES = "m2repo.filter.includes";
    private static final String PROPERTY_NAME_FILTER_EXCLUDES = "m2repo.filter.excludes";

    private boolean keepArtifacts;
    private boolean scanArtifacts;
    private ArtifactFilter artifactFilter;

    /** {@inheritDoc} */
    @Override
    protected void configure() {
        super.configure();
        scanArtifacts = getBooleanProperty(PROPERTY_NAME_ARTIFACTS_SCAN, true);
        keepArtifacts = getBooleanProperty(PROPERTY_NAME_ARTIFACTS_KEEP, true);

        List<String> includeFilter = getFilterPattern(PROPERTY_NAME_FILTER_INCLUDES);
        List<String> excludeFilter = getFilterPattern(PROPERTY_NAME_FILTER_EXCLUDES);
        artifactFilter = new ArtifactFilter(includeFilter, excludeFilter);
    }

    /** {@inheritDoc} */
    @Override
    public boolean accepts(ArtifactInfo item, String path, Scope scope) throws IOException {
        return item != null && MavenScope.REPOSITORY.equals(scope);
    }

    /** {@inheritDoc} */
    @Override
    public RepositoryArtifactDescriptor scan(ArtifactInfo item, String path, Scope scope, Scanner scanner) throws IOException {
        MavenRepositoryDescriptor repositoryDescriptor = scanner.getContext().peek(MavenRepositoryDescriptor.class);
        ArtifactProvider artifactProvider = scanner.getContext().peek(ArtifactProvider.class);
        // register file resolver strategy to identify repository artifacts
        FileResolver fileResolver = scanner.getContext().peek(FileResolver.class);
        fileResolver.push(new RepositoryFileResolverStrategy(artifactProvider.getRepositoryRoot()));
        try {
            return resolveAndScan(scanner, repositoryDescriptor, artifactProvider, item);
        } finally {
            fileResolver.pop();
        }
    }

    /**
     * Resolves, scans and add the artifact to the {@link MavenRepositoryDescriptor}.
     * 
     * @param scanner
     *            the {@link Scanner}
     * @param repoDescriptor
     *            the {@link MavenRepositoryDescriptor}
     * @param artifactProvider
     *            the {@link ArtifactProvider}
     * @param artifactInfo
     *            informations about the searches artifact
     * @throws IOException
     */
    private RepositoryArtifactDescriptor resolveAndScan(Scanner scanner, MavenRepositoryDescriptor repoDescriptor,
            ArtifactProvider artifactProvider, ArtifactInfo artifactInfo) throws IOException {

        PomModelBuilder pomModelBuilder = new EffectiveModelBuilderImpl(artifactProvider);

        Store store = scanner.getContext().getStore();
        String groupId = artifactInfo.getFieldValue(MAVEN.GROUP_ID);
        String artifactId = artifactInfo.getFieldValue(MAVEN.ARTIFACT_ID);
        String classifier = artifactInfo.getFieldValue(MAVEN.CLASSIFIER);
        String packaging = artifactInfo.getFieldValue(MAVEN.PACKAGING);
        String version = artifactInfo.getFieldValue(MAVEN.VERSION);
        long lastModified = artifactInfo.lastModified;
        Artifact artifact = new DefaultArtifact(groupId, artifactId, classifier, packaging, version);

        Artifact modelArtifact = new DefaultArtifact(groupId, artifactId, null, "pom", version);

        if (artifactFilter.match(RepositoryUtils.toArtifact(artifact))) {
            try {
                ArtifactResult modelArtifactResult = artifactProvider.getArtifact(modelArtifact);
                File modelArtifactFile = modelArtifactResult.getArtifact().getFile();
                Model model = pomModelBuilder.getModel(modelArtifactFile);
                DefaultArtifact mainArtifact = new DefaultArtifact(model.getGroupId(), model.getArtifactId(), model.getPackaging(), model
                        .getVersion());
                RepositoryArtifactDescriptor repositoryArtifactDescriptor = getModel(repoDescriptor, mainArtifact, lastModified);
                if (repositoryArtifactDescriptor == null) {
                    scanner.getContext().push(PomModelBuilder.class, pomModelBuilder);
                    FileDescriptor modelDescriptor;
                    try {
                        modelDescriptor = scanner.scan(modelArtifactFile, modelArtifactFile.getAbsolutePath(), null);
                    } finally {
                        scanner.getContext().pop(PomModelBuilder.class);
                    }
                    repositoryArtifactDescriptor = addRepositoryArtifact(repoDescriptor, modelDescriptor, mainArtifact, lastModified, store);
                    if (!keepArtifacts) {
                        modelArtifactFile.delete();
                    }
                }
                if (scanArtifacts && !artifact.equals(modelArtifact)) {
                    ArtifactResult artifactResult = artifactProvider.getArtifact(artifact);
                    File artifactFile = artifactResult.getArtifact().getFile();
                    Descriptor descriptor = scanner.scan(artifactFile, artifactFile.getAbsolutePath(), null);
                    MavenArtifactDescriptor mavenArtifactDescriptor = store.addDescriptorType(descriptor, MavenArtifactDescriptor.class);
                    ArtifactResolver.setCoordinates(mavenArtifactDescriptor, new RepositoryArtifactCoordinates(artifact, lastModified));
                    repositoryArtifactDescriptor.getDescribes().add(mavenArtifactDescriptor);
                    if (!keepArtifacts) {
                        artifactFile.delete();
                    }
                }
                return repositoryArtifactDescriptor;
            } catch (ArtifactResolutionException e) {
                LOGGER.warn(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Returns {@link RepositoryArtifactDescriptor} from the given repository descriptor or <code>null</code>.
     * 
     * @param repositoryDescriptor
     *            the repository in which the artifact will be searched
     * @param artifact
     *            the artifact to find
     * @param lastModified
     *            the last modified date
     * @return a {@link RepositoryArtifactDescriptor} or <code>null</code>.
     */
    private RepositoryArtifactDescriptor getModel(MavenRepositoryDescriptor repositoryDescriptor, Artifact artifact, long lastModified) {
        String coordinates = ArtifactResolver.getId(new ArtifactCoordinates(artifact));
        if (artifact.isSnapshot()) {
            return repositoryDescriptor.getSnapshotArtifact(coordinates, lastModified);
        } else {
            return repositoryDescriptor.getArtifact(coordinates);
        }
    }

    /**
     * Creates a {@link RepositoryArtifactDescriptor} from the given {@link FileDescriptor} and sets some properties.
     * 
     * @param repoDescriptor
     *            the containing repository
     * @param descriptor
     *            the File which contains the artifact
     * @param artifact
     *            the artifact data
     * @param lastModified
     *            last modified date
     * @param store
     *            the store
     * @return the new created artifact descriptor
     */
    private RepositoryArtifactDescriptor addRepositoryArtifact(MavenRepositoryDescriptor repoDescriptor, FileDescriptor descriptor,
            Artifact artifact, long lastModified, Store store) {
        RepositoryArtifactDescriptor artifactDescriptor = store.addDescriptorType(descriptor, RepositoryArtifactDescriptor.class);
        artifactDescriptor.setLastModified(lastModified);
        artifactDescriptor.setContainingRepository(repoDescriptor);
        String coordinates = ArtifactResolver.getId(new ArtifactCoordinates(artifact));
        artifactDescriptor.setMavenCoordinates(coordinates);
        RepositoryArtifactDescriptor lastSnapshot = repoDescriptor.getLastSnapshot(coordinates, lastModified);
        if (lastSnapshot != null) {
            artifactDescriptor.setPredecessorArtifact(lastSnapshot);
            lastSnapshot.setContainingRepository(null);
        }
        return artifactDescriptor;
    }

    /**
     * Extracts a list of artifact filters from the given property.
     * 
     * @param propertyName
     *            The name of the property.
     * @return The list of artifact patterns.
     */
    private List<String> getFilterPattern(String propertyName) {
        String patterns = getStringProperty(propertyName, null);
        if (patterns == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (String pattern : patterns.split(",")) {
            String trimmed = pattern.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

}
