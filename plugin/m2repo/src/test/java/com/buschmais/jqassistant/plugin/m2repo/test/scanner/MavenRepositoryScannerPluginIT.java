package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.javastack.httpd.HttpServer;
import org.junit.Assert;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.api.model.RepositoryArtifactDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenRepositoryScannerPlugin;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

public class MavenRepositoryScannerPluginIT extends AbstractPluginIT {

    private static final int REPO_SERVER_PORT = 8080;
    private static final String REPO_SERVER_BASE_DIR = "./src/test/resources/maven-repository-";

    private static final String TEST_REPOSITORY_URL = "http://localhost:" + REPO_SERVER_PORT;
    private HttpServer httpServer;

    @Override
    protected Map<String, Object> getScannerProperties() {
        return MapBuilder.<String, Object> create("m2repo.directory", "target/m2repo/data").get();
    }

    /**
     * Starts a HTTP server as maven repo.
     * 
     * @throws IOException
     */
    public void startServer(String baseDirSuffix, boolean clearLocalRepo) throws IOException {
        stopServer();
        httpServer = new HttpServer(REPO_SERVER_PORT, REPO_SERVER_BASE_DIR + baseDirSuffix);
        httpServer.start();
        if (clearLocalRepo) {
            File m2Dir = new File(MavenRepositoryScannerPlugin.DEFAULT_M2REPO_DIR);
            if (m2Dir.exists()) {
                FileUtils.deleteDirectory(m2Dir);
            }
        }
    }

    /**
     * Stops the HTTP server.
     * 
     * @throws IOException
     */
    public void stopServer() throws IOException {
        if (httpServer != null) {
            httpServer.stop();
            httpServer = null;
        }
    }

    @Test
    public void testMavenRepoScanner() throws IOException {
        try {
            startServer("1", true);
            store.beginTransaction();
            getScanner().scan(new URL(TEST_REPOSITORY_URL), TEST_REPOSITORY_URL, MavenScope.REPOSITORY);

            Long countJarNodes = store.executeQuery("MATCH (n:Maven:Artifact:Jar) RETURN count(n) as nodes").getSingleResult().get("nodes", Long.class);
            final int expectedJarNodes = 40;
            Assert.assertEquals("Number of jar nodes is wrong.", new Long(expectedJarNodes), countJarNodes);

            MavenRepositoryDescriptor repositoryDescriptor = store.executeQuery("MATCH (n:Maven:Repository) RETURN n").getSingleResult()
                    .get("n", MavenRepositoryDescriptor.class);
            Assert.assertNotNull(repositoryDescriptor);
            Assert.assertEquals(TEST_REPOSITORY_URL, repositoryDescriptor.getUrl());
            final int expectedPomNodes = 9;
            Assert.assertEquals(expectedJarNodes + expectedPomNodes, repositoryDescriptor.getContainedArtifacts().size());
        } finally {
            store.commitTransaction();
            stopServer();
        }
    }

    @Test
    public void testMavenRepoScannerWithUpdate() throws IOException {
        try {
            startServer("2", true);
            store.beginTransaction();
            getScanner().scan(new URL(TEST_REPOSITORY_URL), TEST_REPOSITORY_URL, MavenScope.REPOSITORY);
            Long countArtifactNodes = store.executeQuery("MATCH (n:RepositoryArtifact) RETURN count(n) as nodes").getSingleResult().get("nodes", Long.class);
            Assert.assertEquals("Number of 'RepositoryArtifact' nodes is wrong.", new Long(2), countArtifactNodes);

            startServer("3", false);
            getScanner().scan(new URL(TEST_REPOSITORY_URL), TEST_REPOSITORY_URL, MavenScope.REPOSITORY);

            countArtifactNodes = store.executeQuery("MATCH (n:RepositoryArtifact) RETURN count(n) as nodes").getSingleResult().get("nodes", Long.class);
            Assert.assertEquals("Number of 'RepositoryArtifact' nodes is wrong.", new Long(4), countArtifactNodes);
            // Check relations
            MavenRepositoryDescriptor repositoryDescriptor = store.executeQuery("MATCH (n:Maven:Repository) RETURN n").getSingleResult()
                    .get("n", MavenRepositoryDescriptor.class);
            Assert.assertEquals("Unexpected count of contained Artifacts", 2, repositoryDescriptor.getContainedArtifacts().size());
            for (RepositoryArtifactDescriptor artifact : repositoryDescriptor.getContainedArtifacts()) {
                RepositoryArtifactDescriptor predecessorArtifact = artifact.getPredecessorArtifact();
                Assert.assertNotNull("Predecessor expected.", predecessorArtifact);
                Assert.assertEquals("Equal fqn for artifact and predecessor expected.", artifact.getFullQualifiedName(),
                        predecessorArtifact.getFullQualifiedName());
                Assert.assertTrue(
                        "lastModified date from predecessor not smaller than current artifact modified date (" + predecessorArtifact.getLastModified() + "!<"
                                + artifact.getLastModified() + ")", predecessorArtifact.getLastModified() < artifact.getLastModified());
            }

        } finally {
            store.commitTransaction();
            stopServer();
        }
    }

}
