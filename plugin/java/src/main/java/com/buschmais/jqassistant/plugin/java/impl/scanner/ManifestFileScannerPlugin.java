package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.JavaScope.CLASSPATH;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestEntryDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestSectionDescriptor;

/**
 * Implementation of the
 * {@link com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin}
 * for java MANIFEST.MF files.
 */
public class ManifestFileScannerPlugin extends AbstractScannerPlugin<InputStream> {

    public static final String SECTION_MAIN = "Main";

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super InputStream> getType() {
        return InputStream.class;
    }

    @Override
    public boolean accepts(InputStream item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope) && "/META-INF/MANIFEST.MF".equals(path);
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(InputStream item, String path, Scope scope, Scanner scanner) throws IOException {
        Manifest manifest = new Manifest(item);
        Store store = getStore();
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
        manifestFileDescriptor.setFileName(path);
        return asList(manifestFileDescriptor);
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

}
