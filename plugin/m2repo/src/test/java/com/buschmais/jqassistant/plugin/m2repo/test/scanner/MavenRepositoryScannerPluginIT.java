package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.javastack.httpd.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

public class MavenRepositoryScannerPluginIT extends AbstractPluginIT {

    private static final int REPO_SERVER_PORT = 8080;
    private static final String REPO_SERVER_BASE_DIR = "./src/test/resources/maven-repository";

    private static final String TEST_REPOSITORY_URL = "http://localhost:" + REPO_SERVER_PORT;

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

    /**
     * Starts a HTTP server as maven repo.
     * 
     * @throws IOException
     */
    @Before
    public void startServer() throws IOException {
        getHttpServer().start();
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
    public void testMavenRepoScannerRealRepo() throws MalformedURLException {
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
            Assert.assertEquals(expectedJarNodes, repositoryDescriptor.getContainedArtifacts().size());
        } finally {
            store.commitTransaction();
        }
    }

}
