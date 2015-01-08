package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.MavenRepoCredentials;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.MavenScope;

public class MavenRepositoryScannerPluginIT extends AbstractPluginIT {

    @Test
    public void testMavenRepoScanner() throws MalformedURLException {
        try {
            long beginn = System.currentTimeMillis();
            store.beginTransaction();
            getScanner().scan(new URL(MavenRepoCredentials.REPO_URL), MavenRepoCredentials.REPO_URL, MavenScope.REPOSITORY);
            System.out.println("Dauer: " + (System.currentTimeMillis() - beginn));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            store.commitTransaction();
        }
    }

}
