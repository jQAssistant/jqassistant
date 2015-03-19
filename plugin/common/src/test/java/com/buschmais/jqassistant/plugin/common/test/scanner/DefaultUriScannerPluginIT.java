package com.buschmais.jqassistant.plugin.common.test.scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

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
