package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Verifies file/directory scanning.
 */
class DefaultUriScannerPluginIT extends AbstractPluginIT {

    @Test
    void fileUri() throws URISyntaxException {
        store.beginTransaction();
        URL resource = DefaultUriScannerPluginIT.class.getResource("/");
        URI uri = resource.toURI();
        Descriptor descriptor = getScanner().scan(uri, uri.toString(), DefaultScope.NONE);
        assertThat(descriptor, instanceOf(FileDescriptor.class));
        assertThat(((FileDescriptor) descriptor).getFileName(), equalTo(uri.toString()));
        store.commitTransaction();
    }

    @Test
    void customScope() throws URISyntaxException {
        store.beginTransaction();
        URL resource = DefaultUriScannerPluginIT.class.getResource("/");
        URI uri = resource.toURI();
        Descriptor descriptor = getScanner().scan(uri, uri.toString(), CustomScope.TEST);
        assertThat(descriptor, nullValue());
        store.commitTransaction();
    }

    private enum CustomScope implements Scope {
        TEST;

        @Override
        public String getPrefix() {
            return "custom";
        }

        @Override
        public String getName() {
            return name();
        }
    }
}
