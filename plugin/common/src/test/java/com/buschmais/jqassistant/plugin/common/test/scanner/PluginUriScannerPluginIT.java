package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.net.URI;
import java.net.URISyntaxException;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.URIDescriptor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PluginUriScannerPluginIT extends AbstractPluginIT {

    private static final String DIRECTORY_RESOURCE = "jqassistant-plugin:///test-resource.txt";
    private static final String JAR_RESOURCE = "jqassistant-plugin:///java/lang/Object.class";

    @Test
    void classpathDirectoryPluginResource() throws URISyntaxException {
        verify(DIRECTORY_RESOURCE);
    }

    /**
     * Verify that plugin {@link URI}s are only scanned once.
     */
    @Test
    void idempotentPluginResource() throws URISyntaxException {
        verify(DIRECTORY_RESOURCE);
        assertThat(getScanner().<URI, URIDescriptor>scan(new URI(DIRECTORY_RESOURCE), DIRECTORY_RESOURCE, DefaultScope.NONE)).isNull();
    }

    @Test
    void jarPluginResource() throws URISyntaxException {
        verify(JAR_RESOURCE);
    }

    private void verify(String uri) throws URISyntaxException {
        URI item = new URI(uri);
        URIDescriptor uriDescriptor = getScanner().scan(item, uri, DefaultScope.NONE);
        store.beginTransaction();
        assertThat(uriDescriptor).isNotNull();
        assertThat(uriDescriptor.getUri()).isEqualTo(uri);
        assertThat(uriDescriptor).isInstanceOf(FileDescriptor.class);
        assertThat(((FileDescriptor) uriDescriptor).getFileName()).endsWith(item.getPath());
        store.commitTransaction();
    }

}
