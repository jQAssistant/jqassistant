package com.buschmais.jqassistant.plugin.java.test.scanner;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestSectionDescriptor;

/**
 * Contains tests regarding manifest files.
 */
public class ManifestFileIT extends AbstractPluginIT {

    /**
     * Verifies that manifest files are scanned.
     * 
     * @throws java.io.IOException
     *             If the test fails.
     */
    @Test
    public void manifestFile() throws IOException {
        scanURLs(ManifestFileIT.class.getResource("/META-INF/MANIFEST.MF"));
        store.beginTransaction();
        List<ManifestFileDescriptor> manifestFileDescriptors = query("MATCH (mf:Manifest:File) RETURN mf").getColumn("mf");
        assertThat(manifestFileDescriptors.size(), equalTo(1));

        ManifestFileDescriptor manifestFileDescriptor = manifestFileDescriptors.get(0);
        assertThat(manifestFileDescriptor.getFileName(), endsWith("/META-INF/MANIFEST.MF"));

        List<ManifestSectionDescriptor> manifestSections = query("MATCH (mf:Manifest:File)-[:DECLARES]->(ms:ManifestSection) WHERE ms.name='Main' RETURN ms")
                .getColumn("ms");
        assertThat(manifestSections.size(), equalTo(1));
        store.commitTransaction();
    }
}
