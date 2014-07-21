package com.buschmais.jqassistant.plugin.osgi.impl.scanner;

import static com.buschmais.jqassistant.core.scanner.api.iterable.IterableConsumer.consume;
import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;
import static java.util.Collections.emptyList;

import java.io.File;
import java.io.IOException;

import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.iterable.IterableConsumer;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.AbstractMavenProjectScannerPlugin;

/**
 * Implementation of a {@link ScannerPlugin} for projects build with Apache Felix Bundle Plugin for Maven. 
 * This scanner is only needed if the manifest location of Apache Felix Bundle Plugin is set to another than the default location. 
 */
public class FelixBundleScannerPlugin extends AbstractMavenProjectScannerPlugin {

    @Override
    public boolean accepts(MavenProject item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public Iterable<FileDescriptor> scan(MavenProject project, String path, Scope scope, Scanner scanner) throws IOException {
        
        File manifest = getManifest(project);
        if (manifest != null) {
            Store store = getStore();
            store.beginTransaction();
            try {
                final ArtifactDescriptor artifact = resolveArtifact(project.getArtifact(), false, ArtifactDescriptor.class);
                consume(scanner.scan(manifest, "/META-INF/MANIFEST.MF", CLASSPATH), new IterableConsumer.Consumer<FileDescriptor>() {
                    @Override
                    public void next(FileDescriptor fileDescriptor) {
                         artifact.addContains(fileDescriptor);
                    }
                });
            } finally {
                store.commitTransaction();
            }
        }
        
        return emptyList();
    }

    private File getManifest(MavenProject project) throws IOException {
        File manifest = null;
        
        Xpp3Dom pluginConfig = project.getGoalConfiguration( "org.apache.felix", "maven-bundle-plugin", null, null );
        if (pluginConfig != null) {
            XmlPlexusConfiguration conf = new XmlPlexusConfiguration( pluginConfig );
            String manifestLocation = null;
            try {
                manifestLocation = conf.getChild("manifestLocation").getValue();
            } catch (PlexusConfigurationException e) {
                throw new IOException("Could not read plugin configuration for maven-bundle-blugin", e);
            }

            if (manifestLocation != null) {
                manifest = new File(manifestLocation + "/MANIFEST.MF"); 
                if (!manifest.exists())
                    manifest = null;
            }
        }
        return manifest;
    }

}
