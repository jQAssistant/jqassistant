package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Verifies file/directory scanning.
 */
public class DefaultUriScannerPluginIT extends AbstractPluginIT {

    /**
     * Scan a directory using two dependent plugins for a custom scope.
     *
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void fileUri() throws IOException, URISyntaxException {
        store.beginTransaction();
        URL resource = DefaultUriScannerPluginIT.class.getResource("/");
        URI uri = resource.toURI();
        Descriptor descriptor = getScanner().scan(uri, uri.toString(), DefaultScope.NONE);
        assertThat(descriptor, instanceOf(FileDescriptor.class));
        assertThat(((FileDescriptor)descriptor).getFileName(), equalTo(uri.toString()));
        store.commitTransaction();
    }

}
