package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.javastack.httpd.HttpServer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

@Ignore
public class MavenRepositoryScannerPluginIT extends AbstractPluginIT {

    private static final int REPO_SERVER_PORT = 9090;
    private static final String REPO_SERVER_BASE_DIR = "./src/test/resources/maven-repository-";

    private static final String TEST_REPOSITORY_URL = "http://localhost:" + REPO_SERVER_PORT;
    private HttpServer httpServer;

    private static final String M2REPO_DATA_DIR = "target/m2repo/data";

    protected Map<String, Object> getScannerProperties() {
        return MapBuilder.<String, Object>create("m2repo.directory", M2REPO_DATA_DIR).get();
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
            getScanner(getScannerProperties()).scan(new URL(TEST_REPOSITORY_URL), TEST_REPOSITORY_URL, MavenScope.REPOSITORY);

            Long countJarNodes =
                    store.executeQuery("MATCH (n:Maven:Artifact:Jar) RETURN count(n) as nodes").getSingleResult().get("nodes", Long.class);
            final int expectedJarNodes = 40;
            Assert.assertEquals("Number of jar nodes is wrong.", new Long(expectedJarNodes), countJarNodes);

            MavenRepositoryDescriptor repositoryDescriptor =
                    store.executeQuery("MATCH (r:Maven:Repository) RETURN r").getSingleResult().get("r", MavenRepositoryDescriptor.class);
            Assert.assertNotNull(repositoryDescriptor);
            Assert.assertEquals(TEST_REPOSITORY_URL, repositoryDescriptor.getUrl());
            final int expectedPomNodes = 9;
            Assert.assertEquals(expectedPomNodes, repositoryDescriptor.getContainedArtifacts().size());
        } finally {
            store.commitTransaction();
            stopServer();
        }
    }

    @Test
    public void testMavenRepoScannerWithUpdate() throws IOException {
        try {
            startServer("2");
            store.beginTransaction();
            getScanner(getScannerProperties()).scan(new URL(TEST_REPOSITORY_URL), TEST_REPOSITORY_URL, MavenScope.REPOSITORY);
            Long countArtifactNodes =
                    store.executeQuery("MATCH (n:RepositoryArtifact:Maven:Pom:Xml) RETURN count(n) as nodes").getSingleResult().get("nodes",
                            Long.class);
            Assert.assertEquals("Number of 'RepositoryArtifact' nodes is wrong.", new Long(1), countArtifactNodes);

            startServer("3");
            getScanner(getScannerProperties()).scan(new URL(TEST_REPOSITORY_URL), TEST_REPOSITORY_URL, MavenScope.REPOSITORY);

            countArtifactNodes =
                    store.executeQuery("MATCH (n:RepositoryArtifact:Maven:Pom:Xml) RETURN count(n) as nodes").getSingleResult().get("nodes",
                            Long.class);
            Assert.assertEquals("Number of 'RepositoryArtifact' nodes is wrong.", new Long(2), countArtifactNodes);
        } finally {
            store.commitTransaction();
            stopServer();
        }
    }
}
