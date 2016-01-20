package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.MAVEN;
import org.javastack.httpd.HttpServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.m2repo.api.ArtifactProvider;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.AetherArtifactProvider;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;
import com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact.MavenArtifactResolver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class MavenArtifactScannerPluginIT extends AbstractPluginIT {

    private static final int REPO_SERVER_PORT = 9095;
    private static final String REPO_SERVER_BASE_DIR = "./src/test/resources/maven-repository-";

    private static final String TEST_REPOSITORY_URL = "http://localhost:" + REPO_SERVER_PORT;
    private HttpServer httpServer;

    private static final String M2REPO_DATA_DIR = "target/m2repo/data";

    protected Map<String, Object> getScannerProperties() {
        return MapBuilder.<String, Object> create("m2repo.directory", M2REPO_DATA_DIR).get();
    }

    /**
     * Starts a HTTP server as maven repo.
     * 
     * @throws IOException
     */
    private void startServer(String baseDirSuffix) throws IOException {
        stopServer();
        httpServer = new HttpServer(REPO_SERVER_PORT, REPO_SERVER_BASE_DIR + baseDirSuffix);
        httpServer.start();
    }

    /**
     * Stops the HTTP server.
     * 
     * @throws IOException
     */
    private void stopServer() throws IOException {
        if (httpServer != null) {
            httpServer.stop();
            httpServer = null;
        }
    }

    /**
     * Removes the DEFAULT_M2REPO_DIR if existent.
     * 
     * @throws IOException
     */
    @Before
    public void clearLocalRepo() throws IOException {
        File m2Dir = new File(M2REPO_DATA_DIR);
        if (m2Dir.exists()) {
            FileUtils.deleteDirectory(m2Dir);
        }
    }

    @Test
    public void testMavenRepoScanner() throws IOException {
        try {
            startServer("1");
            store.beginTransaction();

            ArtifactInfo info = new ArtifactInfo();
            info.setFieldValue(MAVEN.GROUP_ID, "com.buschmais.xo");
            info.setFieldValue(MAVEN.ARTIFACT_ID, "xo.api");
            info.setFieldValue(MAVEN.VERSION, "0.5.0-SNAPSHOT");
            info.setFieldValue(MAVEN.PACKAGING, "jar");

            Scanner scanner = getScanner(getScannerProperties());

            MavenRepositoryDescriptor repoDescriptor = store.create(MavenRepositoryDescriptor.class);
            ArtifactProvider provider = new AetherArtifactProvider(new URL(TEST_REPOSITORY_URL), repoDescriptor, new File(M2REPO_DATA_DIR));
            ScannerContext context = scanner.getContext();
            context.push(ArtifactProvider.class, provider);
            context.push(ArtifactResolver.class, new MavenArtifactResolver());
            repoDescriptor.setUrl(TEST_REPOSITORY_URL);
            scanner.scan(info, info.toString(), MavenScope.REPOSITORY);
            context.pop(ArtifactProvider.class);
            context.pop(ArtifactResolver.class);

            Long countJarNodes = store.executeQuery("MATCH (n:Maven:Artifact:Jar) RETURN count(n) as nodes").getSingleResult().get("nodes", Long.class);
            assertThat("Number of jar nodes is wrong.", countJarNodes, equalTo(1L));

            MavenRepositoryDescriptor repositoryDescriptor = store.executeQuery("MATCH (r:Maven:Repository) RETURN r").getSingleResult()
                    .get("r", MavenRepositoryDescriptor.class);
            assertThat(repoDescriptor, not(nullValue()));
            assertThat(repoDescriptor.getContainedModels(), hasSize(1));
        } finally {
            store.commitTransaction();
            stopServer();
        }
    }
}
