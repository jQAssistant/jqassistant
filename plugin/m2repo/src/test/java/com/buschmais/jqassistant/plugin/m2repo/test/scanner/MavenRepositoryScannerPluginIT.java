package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

public class MavenRepositoryScannerPluginIT extends AbstractPluginIT {

    private static final String MAVEN_CENTRAL_URL = "http://repo1.maven.org/maven2/";

    @Test
    public void testMavenRepoScannerRealRepo() throws MalformedURLException {
        try {
            store.beginTransaction();
            getScanner().scan(new URL(MAVEN_CENTRAL_URL), MAVEN_CENTRAL_URL, MavenScope.REPOSITORY);
        } finally {
            store.commitTransaction();
        }
    }

}
