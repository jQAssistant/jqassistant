package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.m2repo.api.ArtifactProvider;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenReleaseDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenSnapshotDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactHelper;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.PomModelBuilder;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.MAVEN;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin for (remote) maven artifacts.
 * 
 * @author pherklotz
 */
public class MavenArtifactScannerPlugin extends AbstractScannerPlugin<ArtifactInfo, MavenArtifactDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenArtifactScannerPlugin.class);
    private static final String PROPERTY_NAME_ARTIFACTS_KEEP = "m2repo.artifacts.keep";
    private static final String PROPERTY_NAME_ARTIFACTS_SCAN = "m2repo.artifacts.scan";
    private static final String PROPERTY_NAME_FILTER_INCLUDES = "m2repo.filter.includes";
    private static final String PROPERTY_NAME_FILTER_EXCLUDES = "m2repo.filter.excludes";

    private boolean keepArtifacts;
    private boolean scanArtifacts;
    private ArtifactFilter artifactFilter;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configure() {
        super.configure();
        scanArtifacts = getBooleanProperty(PROPERTY_NAME_ARTIFACTS_SCAN, true);
        keepArtifacts = getBooleanProperty(PROPERTY_NAME_ARTIFACTS_KEEP, true);

        List<String> includeFilter = getFilterPattern(PROPERTY_NAME_FILTER_INCLUDES);
        List<String> excludeFilter = getFilterPattern(PROPERTY_NAME_FILTER_EXCLUDES);
        artifactFilter = new ArtifactFilter(includeFilter, excludeFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accepts(ArtifactInfo item, String path, Scope scope) throws IOException {
        return item != null && MavenScope.REPOSITORY.equals(scope);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MavenArtifactDescriptor scan(ArtifactInfo item, String path, Scope scope, Scanner scanner) throws IOException {
        ArtifactProvider artifactProvider = scanner.getContext().peek(ArtifactProvider.class);
        // register file resolver strategy to identify repository artifacts
        scanner.getContext().push(FileResolver.class, artifactProvider.getFileResolver());
        scanner.getContext().push(ArtifactResolver.class, artifactProvider.getArtifactResolver());
        try {
            return resolveAndScan(scanner, artifactProvider, item);
        } finally {
            scanner.getContext().pop(ArtifactResolver.class);
            scanner.getContext().pop(FileResolver.class);
        }
    }

    /**
     * Resolves, scans and add the artifact to the
     * {@link MavenRepositoryDescriptor}.
     *
     * @param scanner
     *            the {@link Scanner}
     * @param artifactProvider
     *            the {@link AetherArtifactProvider}
     * @param artifactInfo
     *            informations about the searches artifact
     * @throws IOException
     */
    private MavenArtifactDescriptor resolveAndScan(Scanner scanner, ArtifactProvider artifactProvider,
                                                   ArtifactInfo artifactInfo)
        throws IOException {

        PomModelBuilder pomModelBuilder = new EffectiveModelBuilder(artifactProvider);

        ScannerContext context = scanner.getContext();
        Store store = context.getStore();
        String groupId = artifactInfo.getFieldValue(MAVEN.GROUP_ID);
        String artifactId = artifactInfo.getFieldValue(MAVEN.ARTIFACT_ID);
        String classifier = artifactInfo.getFieldValue(MAVEN.CLASSIFIER);
        String packaging = artifactInfo.getFieldValue(MAVEN.PACKAGING);
        String version = artifactInfo.getFieldValue(MAVEN.VERSION);
        long lastModified = artifactInfo.lastModified;
        Artifact artifact = new DefaultArtifact(groupId, artifactId, classifier, packaging, version);

        if (artifactFilter.match(RepositoryUtils.toArtifact(artifact))) {
            try {
                DefaultArtifact defaultArtifact = new DefaultArtifact(groupId, artifactId, null, "pom", version);
                ArtifactResult modelArtifactResult = artifactProvider.getArtifact(defaultArtifact);
                Artifact resolvedModelArtifact = modelArtifactResult.getArtifact();
                MavenRepositoryDescriptor repositoryDescriptor = artifactProvider.getRepositoryDescriptor();
                MavenPomXmlDescriptor modelDescriptor = findModel(repositoryDescriptor, resolvedModelArtifact);
                if (modelDescriptor == null) {
                    File modelArtifactFile = resolvedModelArtifact.getFile();
                    context.push(PomModelBuilder.class, pomModelBuilder);
                    try {
                        modelDescriptor = scanner.scan(modelArtifactFile, modelArtifactFile.getAbsolutePath(), null);
                    } finally {
                        context.pop(PomModelBuilder.class);
                        if (!keepArtifacts) {
                            modelArtifactFile.delete();
                        }
                    }
                    modelDescriptor = markReleaseOrSnaphot(modelDescriptor, MavenPomXmlDescriptor.class,
                                                           resolvedModelArtifact, lastModified, store);
                    repositoryDescriptor.getContainedModels().add(modelDescriptor);
                }
                if (scanArtifacts && !artifact.getExtension().equals("pom")) {
                    ArtifactResult artifactResult = artifactProvider.getArtifact(artifact);
                    File artifactFile = artifactResult.getArtifact().getFile();
                    Descriptor descriptor;
                    try {
                        descriptor = scanner.scan(artifactFile, artifactFile.getAbsolutePath(), null);
                    } finally {
                        if (!keepArtifacts) {
                            artifactFile.delete();
                        }
                    }
                    MavenArtifactDescriptor descriptorToAdd = store.addDescriptorType(descriptor, MavenArtifactDescriptor.class);
                    MavenArtifactDescriptor mavenArtifactDescriptor = markReleaseOrSnaphot(descriptorToAdd,
                                                                                           MavenArtifactDescriptor.class,
                                                                                           artifact, lastModified, store);
                    MavenArtifactHelper.setId(mavenArtifactDescriptor, new RepositoryArtifactCoordinates(artifact, lastModified));
                    MavenArtifactHelper.setCoordinates(mavenArtifactDescriptor, new RepositoryArtifactCoordinates(artifact, lastModified));
                    modelDescriptor.getDescribes().add(mavenArtifactDescriptor);
                    repositoryDescriptor.getContainedArtifacts().add(mavenArtifactDescriptor);
                    return mavenArtifactDescriptor;
                }
            } catch (ArtifactResolutionException e) {
                LOGGER.warn(e.getMessage());
            }
        }
        return null;
    }

    /**
     * Returns {@link MavenPomXmlDescriptor} from the given repository
     * descriptor or <code>null</code>.
     * 
     * @param repositoryDescriptor
     *            the repository containing the model.
     * @param resolvedModelArtifact
     *            The resolved model artifact (i.e. in case of a snapshot
     *            containing the timestamp/buildnumber in the version.)
     * @return a {@link MavenPomXmlDescriptor} or `null`.
     */
    private MavenPomXmlDescriptor findModel(MavenRepositoryDescriptor repositoryDescriptor, Artifact resolvedModelArtifact) {
        Artifact resolvedMainArtifact = new DefaultArtifact(resolvedModelArtifact.getGroupId(), resolvedModelArtifact.getArtifactId(),
                resolvedModelArtifact.getExtension(), resolvedModelArtifact.getVersion());
        String coordinates = MavenArtifactHelper.getId(new ArtifactCoordinates(resolvedMainArtifact));
        return repositoryDescriptor.findModel(coordinates);
    }

    /**
     * Adds a `Release` or `Snapshot` label to the given maven descriptor
     * depending on the artifact version type.
     * 
     * @param descriptor
     *            the descriptor
     * @param type
     *            the expected descriptor type
     * @param resolvedArtifact
     *            the resolved artifact
     * @param lastModified
     *            last modified date (for snapshots only)
     * @param store
     *            the store
     * @return the new created resolvedArtifact descriptor
     */
    private <D extends MavenDescriptor> D markReleaseOrSnaphot(D descriptor, Class<? extends D> type, Artifact resolvedArtifact, long lastModified,
            Store store) {
        if (resolvedArtifact.isSnapshot()) {
            MavenSnapshotDescriptor snapshotDescriptor = store.addDescriptorType(descriptor, MavenSnapshotDescriptor.class);
            snapshotDescriptor.setLastModified(lastModified);
            return type.cast(snapshotDescriptor);
        } else {
            return store.addDescriptorType(descriptor, MavenReleaseDescriptor.class, type);
        }
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
