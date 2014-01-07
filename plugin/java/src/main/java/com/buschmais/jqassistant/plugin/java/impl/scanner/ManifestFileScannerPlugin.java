package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ManifestFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.plugin.java.impl.store.visitor.ClassVisitor;
import com.buschmais.jqassistant.plugin.java.impl.store.visitor.VisitorHelper;
import org.objectweb.asm.ClassReader;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.jar.Manifest;

/**
 * Implementation of the
 * {@link FileScannerPlugin} for java
 * MANIFEST.MF files.
 */
public class ManifestFileScannerPlugin implements FileScannerPlugin<ManifestFileDescriptor> {

	@Override
	public boolean matches(String file, boolean isDirectory) {
		return !isDirectory && file.equals("META-INF/MANIFEST.MF");
	}

	@Override
	public ManifestFileDescriptor scanFile(Store store, StreamSource streamSource) throws IOException {
        Manifest manifest = new Manifest(streamSource.getInputStream());
		return null;
	}

	@Override
	public ManifestFileDescriptor scanDirectory(Store store, String name) throws IOException {
		return null;
	}
}
