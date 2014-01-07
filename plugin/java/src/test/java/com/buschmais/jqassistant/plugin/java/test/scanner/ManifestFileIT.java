package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestFileDescriptor;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Contains tests regarding manifest files.
 */
public class ManifestFileIT extends AbstractPluginIT {

    /**
     * Verifies that manifest files are scanned.
     *
     * @throws java.io.IOException If the test fails.
     */
    @Test
    public void propertyFile() throws IOException {
        scanURLs(ManifestFileIT.class.getResource("/META-INF/MANIFEST.MF"));
        store.beginTransaction();
        List<ManifestFileDescriptor> manifestFileDescriptors = query("MATCH (mf:MANIFESTFILE) RETURN mf").getColumn("mf");
        assertThat(manifestFileDescriptors.size(), equalTo(1));
        store.commitTransaction();
    }
}
