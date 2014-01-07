package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestEntryDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestSectionDescriptor;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Implementation of the
 * {@link FileScannerPlugin} for java
 * MANIFEST.MF files.
 */
public class ManifestFileScannerPlugin implements FileScannerPlugin<ManifestFileDescriptor> {

    public static final String SECTION_MAIN = "Main";

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return !isDirectory && file.endsWith("META-INF/MANIFEST.MF");
    }

	@Override
	public ManifestFileDescriptor scanFile(Store store, StreamSource streamSource) throws IOException {
        Manifest manifest = new Manifest(streamSource.getInputStream());
        ManifestFileDescriptor manifestFileDescriptor = store.create(ManifestFileDescriptor.class);
        ManifestSectionDescriptor mainSectionDescriptor = store.create(ManifestSectionDescriptor.class);
        mainSectionDescriptor.setName(SECTION_MAIN);
        manifestFileDescriptor.setMainSection(mainSectionDescriptor);
        readSection(manifest.getMainAttributes(), mainSectionDescriptor, store);
        for (Map.Entry<String, Attributes> sectionEntry : manifest.getEntries().entrySet()) {
            ManifestSectionDescriptor sectionDescriptor = store.create(ManifestSectionDescriptor.class);
            sectionDescriptor.setName(sectionEntry.getKey());
            readSection(sectionEntry.getValue(), sectionDescriptor, store);
            manifestFileDescriptor.getManifestSections().add(sectionDescriptor);
        }
        return manifestFileDescriptor;
    }

    private void readSection(Attributes attributes, ManifestSectionDescriptor sectionDescriptor, Store store) {
        for (Map.Entry<Object, Object> entry : attributes.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            ManifestEntryDescriptor entryDescriptor = store.create(ManifestEntryDescriptor.class);
            entryDescriptor.setName(key != null ? key.toString() : null);
            entryDescriptor.setValue(value != null ? value.toString() : null);
            sectionDescriptor.getManifestEntries().add(entryDescriptor);
        }
    }

    @Override
    public ManifestFileDescriptor scanDirectory(Store store, String name) throws IOException {
		return null;
	}
}
