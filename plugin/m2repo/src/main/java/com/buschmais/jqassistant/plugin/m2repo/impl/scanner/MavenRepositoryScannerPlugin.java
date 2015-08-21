package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.index.ArtifactInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

/**
 * A scanner for (remote) maven repositories.
 * 
 * @author pherklotz
 */
public class MavenRepositoryScannerPlugin extends AbstractScannerPlugin<URL, MavenRepositoryDescriptor> {

    public static final String DEFAULT_M2REPO_DIR = "./jqassistant/data/m2repo";

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenRepositoryScannerPlugin.class);

    private static final String PROPERTY_NAME_DIRECTORY = "m2repo.directory";

    private File localDirectory;

    /** {@inheritDoc} */
    @Override
    public boolean accepts(URL item, String path, Scope scope) throws IOException {
        return MavenScope.REPOSITORY == scope;
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
        File workDirectory = new File(localDirectory, DigestUtils.md5Hex(item.toString()));
        File repositoryRoot = new File(workDirectory, "repository");
        File indexRoot = new File(workDirectory, "index");
        // handles the remote maven index
        MavenIndex mavenIndex = new MavenIndex(item, repositoryRoot, indexRoot, username, password);
        // used to resolve (remote) artifacts
        ArtifactProvider artifactProvider = new ArtifactProvider(item, repositoryRoot, username, password);
        // register file resolver strategy to identify repository artifacts
        FileResolver fileResolver = scanner.getContext().peek(FileResolver.class);
        fileResolver.push(new RepositoryFileResolverStrategy(repositoryRoot));
        try {
            return scanRepository(item, scanner, mavenIndex, artifactProvider);
        } finally {
            fileResolver.pop();
        }
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
    public MavenRepositoryDescriptor scanRepository(URL item, Scanner scanner, MavenIndex mavenIndex, ArtifactProvider artifactProvider) throws IOException {
        ScannerContext context = scanner.getContext();
        Store store = context.getStore();
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
        try {
            context.push(MavenRepositoryDescriptor.class, repoDescriptor);
            context.push(ArtifactProvider.class, artifactProvider);

            Iterable<ArtifactInfo> searchResponse = mavenIndex.getArtifactsSince(artifactsSince);
            for (ArtifactInfo ai : searchResponse) {
                scanner.scan(ai, ai.toString(), MavenScope.REPOSITORY);
            }
        } finally {
            context.pop(MavenRepositoryDescriptor.class);
            context.pop(ArtifactProvider.class);
        }
        mavenIndex.closeCurrentIndexingContext();
        repoDescriptor.setLastScanDate(System.currentTimeMillis());
        return repoDescriptor;
    }
}
