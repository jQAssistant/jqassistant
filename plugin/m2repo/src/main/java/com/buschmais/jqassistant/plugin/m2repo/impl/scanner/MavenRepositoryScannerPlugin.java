package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.RepositoryArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.ArtifactResolver;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A scanner for (remote) maven repositories.
 * 
 * @author pherklotz
 */
public class MavenRepositoryScannerPlugin extends AbstractScannerPlugin<URL, MavenRepositoryDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenRepositoryScannerPlugin.class);

    private static final String PROPERTY_NAME_DIRECTORY = "m2repo.directory";
    private static final String PROPERTY_NAME_ARTIFACTS_KEEP = "m2repo.artifacts.keep";
    private static final String PROPERTY_NAME_ARTIFACTS_SCAN = "m2repo.artifacts.scan";
    private static final String PROPERTY_NAME_FILTER_INCLUDES = "m2repo.filter.includes";
    private static final String PROPERTY_NAME_FILTER_EXCLUDES = "m2repo.filter.excludes";

    public static final String DEFAULT_M2REPO_DIR = "./jqassistant/data/m2repo";

    private File localDirectory;

    private boolean keepArtifacts;
    private boolean scanArtifacts;
    private List<String> includeFilter;
    private List<String> excludeFilter;

    /** {@inheritDoc} */
    @Override
    public boolean accepts(URL item, String path, Scope scope) throws IOException {
        return MavenScope.REPOSITORY == scope;
    }

    /**
     * Returns a string with the artifact coordinates (groupId:artifactId:[classifier:]version).
     * 
     * @param artifact
     *            the artifact
     * @return a string with the artifact coordinates (groupId:artifactId:[classifier:]version).
     */
    private String getCoordinates(Artifact artifact) {
        StringBuilder builder = new StringBuilder(artifact.getGroupId()).append(":");
        builder.append(artifact.getArtifactId()).append(":");
        builder.append(artifact.getVersion());
        builder.append(artifact.getExtension()).append(":");
        if (StringUtils.isNotBlank(artifact.getClassifier())) {
            builder.append(artifact.getClassifier()).append(":");
        }
        return builder.toString();
    }

    /**
     * Finds or creates a repository descriptor for the given url.
     * 
     * @param store
     *            the {@link Store}
     * @param url
     *            the repository url
     * @return a {@link MavenRepositoryDescriptor} for the given url.
     */
    private MavenRepositoryDescriptor getRepositoryDescriptor(Store store, String url) {
        MavenRepositoryDescriptor repositoryDescriptor = store.find(MavenRepositoryDescriptor.class, url);
        if (repositoryDescriptor == null) {
            repositoryDescriptor = store.create(MavenRepositoryDescriptor.class);
            repositoryDescriptor.setUrl(url);
        }
        return repositoryDescriptor;
    }

    /** {@inheritDoc} */
    @Override
    public void configure() {
        localDirectory = new File(getStringProperty(PROPERTY_NAME_DIRECTORY, DEFAULT_M2REPO_DIR));
        scanArtifacts = getBooleanProperty(PROPERTY_NAME_ARTIFACTS_SCAN, true);
        keepArtifacts = getBooleanProperty(PROPERTY_NAME_ARTIFACTS_KEEP, true);
        includeFilter = getFilterPattern(PROPERTY_NAME_FILTER_INCLUDES);
        excludeFilter = getFilterPattern(PROPERTY_NAME_FILTER_EXCLUDES);
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

    /**
     * Resolves, scans and add the artifact to the {@link MavenRepositoryDescriptor}.
     * 
     * @param scanner
     *            the {@link Scanner}
     * @param repoDescriptor
     *            the {@link MavenRepositoryDescriptor}
     * @param pomModelBuilder
     *            the {@link PomModelBuilder}
     * @param artifactProvider
     *            the {@link ArtifactProvider}
     * @param artifactFilter
     *            The {@link ArtifactFilter}.
     * @param artifactInfo
     *            informations about the searches artifact
     * @throws IOException
     */
    private void resolveAndScan(Scanner scanner, MavenRepositoryDescriptor repoDescriptor, ArtifactProvider artifactProvider,
            PomModelBuilder pomModelBuilder, ArtifactFilter artifactFilter, ArtifactInfo artifactInfo) throws IOException {
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
                DefaultArtifact mainArtifact =
                        new DefaultArtifact(model.getGroupId(), model.getArtifactId(), model.getPackaging(), model.getVersion());
                MavenArtifactDescriptor mavenArtifactDescriptor = null;
                if (scanArtifacts && !artifact.equals(modelArtifact)) {
                    ArtifactResult artifactResult = artifactProvider.getArtifact(artifact);
                    File artifactFile = artifactResult.getArtifact().getFile();
                    Descriptor descriptor = scanner.scan(artifactFile, artifactFile.getAbsolutePath(), null);
                    mavenArtifactDescriptor = store.addDescriptorType(descriptor, MavenArtifactDescriptor.class);
                    mavenArtifactDescriptor.setGroup(groupId);
                    mavenArtifactDescriptor.setName(artifactId);
                    mavenArtifactDescriptor.setVersion(version);
                    mavenArtifactDescriptor.setClassifier(classifier);
                    mavenArtifactDescriptor.setType(packaging);
                    mavenArtifactDescriptor.setFullQualifiedName(ArtifactResolver.createId(new ArtifactResolver.ArtifactCoordinates(
                            RepositoryUtils.toArtifact(artifact), false)));
                    if (!keepArtifacts) {
                        artifactFile.delete();
                    }
                }
                RepositoryArtifactDescriptor repositoryArtifactDescriptor = getModel(repoDescriptor, mainArtifact, lastModified);
                if (repositoryArtifactDescriptor == null) {
                    MavenPomXmlDescriptor modelDescriptor = scanner.scan(model, modelArtifactFile.getAbsolutePath(), null);
                    repositoryArtifactDescriptor = addRepositoryArtifact(repoDescriptor, modelDescriptor, mainArtifact, lastModified, store);
                    if (!keepArtifacts) {
                        modelArtifactFile.delete();
                    }
                }
                if (mavenArtifactDescriptor != null) {
                    repositoryArtifactDescriptor.getDescribes().add(mavenArtifactDescriptor);
                }
            } catch (ArtifactResolutionException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    private RepositoryArtifactDescriptor getModel(MavenRepositoryDescriptor repositoryDescriptor, Artifact artifact, long lastModified) {
        String coordinates = getCoordinates(artifact);
        if (artifact.isSnapshot()) {
            return repositoryDescriptor.getSnapshotArtifact(coordinates, lastModified);
        } else {
            return repositoryDescriptor.getArtifact(coordinates);
        }
    }

    private RepositoryArtifactDescriptor addRepositoryArtifact(MavenRepositoryDescriptor repoDescriptor, MavenPomXmlDescriptor descriptor,
            Artifact artifact, long lastModified, Store store) {
        RepositoryArtifactDescriptor artifactDescriptor = store.addDescriptorType(descriptor, RepositoryArtifactDescriptor.class);
        artifactDescriptor.setLastModified(lastModified);
        artifactDescriptor.setContainingRepository(repoDescriptor);
        String coordinates = getCoordinates(artifact);
        artifactDescriptor.setMavenCoordinates(coordinates);
        RepositoryArtifactDescriptor lastSnapshot = repoDescriptor.getLastSnapshot(coordinates, lastModified);
        if (lastSnapshot != null) {
            artifactDescriptor.setPredecessorArtifact(lastSnapshot);
            lastSnapshot.setContainingRepository(null);
        }
        return artifactDescriptor;
    }

    /** {@inheritDoc} */
    @Override
    public MavenRepositoryDescriptor scan(URL item, String path, Scope scope, Scanner scanner) throws IOException {
        String userInfo = item.getUserInfo();
        String username = StringUtils.substringBefore(userInfo, ":");
        String password = StringUtils.substringAfter(userInfo, ":");
        if (!localDirectory.exists()) {
            LOGGER.info("Creating local maven repository directory {}", localDirectory.getAbsolutePath());
            localDirectory.mkdirs();
        }
        File localRepoDir = new File(localDirectory, DigestUtils.md5Hex(item.toString()));
        // handles the remote maven index
        MavenIndex mavenIndex = new MavenIndex(item, localRepoDir, username, password);
        // used to resolve (remote) artifacts
        ArtifactProvider artifactProvider = new ArtifactProvider(item, localRepoDir, username, password);
        PomModelBuilder pomModelBuilder = new PomModelBuilder(artifactProvider);
        ArtifactFilter artifactFilter = new ArtifactFilter(includeFilter, excludeFilter);
        return scanRepository(item, scanner, mavenIndex, artifactProvider, pomModelBuilder, artifactFilter);
    }

    /**
     * Scans a Repository.
     * 
     * @param item
     *            the URL
     * @param scanner
     *            the Scanner
     * @param mavenIndex
     *            the MavenIndex
     * @param artifactProvider
     *            the ArtifactResolver
     * @param pomModelBuilder
     *            the PomModelBuilder
     * @param artifactFilter
     *            The artifact filter to apply.
     * @return a MavenRepositoryDescriptor
     * @throws IOException
     */
    public MavenRepositoryDescriptor scanRepository(URL item, Scanner scanner, MavenIndex mavenIndex, ArtifactProvider artifactProvider,
            PomModelBuilder pomModelBuilder, ArtifactFilter artifactFilter) throws IOException {

        Store store = scanner.getContext().getStore();
        // the MavenRepositoryDescriptor
        MavenRepositoryDescriptor repoDescriptor = getRepositoryDescriptor(store, item.toString());

        Date lastIndexUpdateTime = mavenIndex.getLastUpdateLocalRepo();
        Date lastScanTime = new Date(repoDescriptor.getLastScanDate());
        Date artifactsSince = lastIndexUpdateTime;
        if (lastIndexUpdateTime == null || lastIndexUpdateTime.after(lastScanTime)) {
            artifactsSince = lastScanTime;
        }

        mavenIndex.updateIndex();

        // Search artifacts
        Iterable<ArtifactInfo> searchResponse = mavenIndex.getArtifactsSince(artifactsSince);
        for (ArtifactInfo ai : searchResponse) {
            resolveAndScan(scanner, repoDescriptor, artifactProvider, pomModelBuilder, artifactFilter, ai);
        }
        mavenIndex.closeCurrentIndexingContext();
        repoDescriptor.setLastScanDate(System.currentTimeMillis());
        return repoDescriptor;
    }
}
