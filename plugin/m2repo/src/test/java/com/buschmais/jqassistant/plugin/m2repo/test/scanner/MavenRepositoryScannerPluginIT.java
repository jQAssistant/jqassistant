package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.javastack.httpd.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.test.scanner.MapBuilder;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenRepositoryScannerPlugin;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

public class MavenRepositoryScannerPluginIT extends AbstractPluginIT {

    private static final int REPO_SERVER_PORT = 8080;
    private static final String REPO_SERVER_BASE_DIR = "./src/test/resources/maven-repository";

    // private static final String TEST_REPOSITORY_URL = "http://localhost:" +
    // REPO_SERVER_PORT;
    private static final String TEST_REPOSITORY_URL = "http://drs-repo.asml.com/artifactory/libs-release-local";
    // http://drs-repo.asml.com/artifactory/libs-release-local/
    private HttpServer httpServer;

    /**
     * Lazy Getter.
     * 
     * @return a HttpServer
     * @throws IOException
     */
    private HttpServer getHttpServer() throws IOException {
        if (httpServer == null) {
            httpServer = new HttpServer(REPO_SERVER_PORT, REPO_SERVER_BASE_DIR);
        }

        return httpServer;
    }

    @Override
    protected Map<String, Object> getScannerProperties() {
        return MapBuilder.<String, Object> create("m2repo.directory", "target/m2repo/data").get();
    }

    /**
     * Starts a HTTP server as maven repo.
     * 
     * @throws IOException
     */
    @Before
    public void startServer() throws IOException {
        getHttpServer().start();
        File m2Dir = new File(MavenRepositoryScannerPlugin.DEFAULT_M2REPO_DIR);
        if (m2Dir.exists()) {
            FileUtils.deleteDirectory(m2Dir);
        }
    }

    /**
     * Stops the HTTP server.
     * 
     * @throws IOException
     */
    @After
    public void stopServer() throws IOException {
        getHttpServer().stop();
    }

    @Test
    public void testMavenRepoScanner() throws MalformedURLException {
        try {
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
        }
    }

}
