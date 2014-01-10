package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestSectionDescriptor;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.endsWith;
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
    public void manifestFile() throws IOException {
        scanURLs(ManifestFileIT.class.getResource("/META-INF/MANIFEST.MF"));
        store.beginTransaction();
        List<ManifestFileDescriptor> manifestFileDescriptors = query("MATCH (mf:MANIFEST:FILE) RETURN mf").getColumn("mf");
        assertThat(manifestFileDescriptors.size(), equalTo(1));

        ManifestFileDescriptor manifestFileDescriptor = manifestFileDescriptors.get(0);
        assertThat(manifestFileDescriptor.getFileName(), endsWith("/META-INF/MANIFEST.MF"));

        List<ManifestSectionDescriptor> manifestSections = query("MATCH (mf:MANIFEST:FILE)-[:DECLARES]->(ms:MANIFESTSECTION) WHERE ms.NAME='Main' RETURN ms").getColumn("ms");
        assertThat(manifestSections.size(), equalTo(1));
        store.commitTransaction();
    }
}
