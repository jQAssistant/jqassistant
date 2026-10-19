package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.api.model.ManifestFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ManifestSectionDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains tests regarding manifest files.
 */
class ManifestFileIT extends AbstractJavaPluginIT {

    /**
     * Verifies that manifest files are scanned.
     *
     */
    @Test
    void manifestFile() {
        scanClassPathResource(JavaScope.CLASSPATH, "/META-INF/MANIFEST.MF");
        store.beginTransaction();
        List<ManifestFileDescriptor> manifestFileDescriptors = query("MATCH (mf:Manifest:File) RETURN mf").getColumn("mf");
        assertThat(manifestFileDescriptors.size()).isEqualTo(1);

        ManifestFileDescriptor manifestFileDescriptor = manifestFileDescriptors.get(0);
        assertThat(manifestFileDescriptor.getFileName()).endsWith("/META-INF/MANIFEST.MF");

        List<ManifestSectionDescriptor> manifestSections = query("MATCH (mf:Manifest:File)-[:DECLARES]->(ms:ManifestSection) WHERE ms.name='Main' RETURN ms")
                .getColumn("ms");
        assertThat(manifestSections.size()).isEqualTo(1);
        store.commitTransaction();
    }
}
