package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.net.URI;
import java.net.URISyntaxException;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JQAssistantPluginScannerPluginIT extends AbstractPluginIT {

    @Test
    void classpathDirectoryPluginResource() throws URISyntaxException {
        verify("jqassistant-plugin:/test-resource.txt");
    }

    @Test
    void jarPluginResource() throws URISyntaxException {
        verify("jqassistant-plugin:/java/lang/Object.class");
    }

    private void verify(String uri) throws URISyntaxException {
        FileDescriptor fileDescriptor = getScanner().scan(new URI(uri), uri, DefaultScope.NONE);
        store.beginTransaction();
        assertThat(fileDescriptor).isNotNull();
        assertThat(fileDescriptor.getFileName()).isEqualTo(uri);
        store.commitTransaction();
    }

}
