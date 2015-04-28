package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Scans pom.xml files.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
public abstract class AbstractMavenPomScannerPlugin extends AbstractScannerPlugin<FileResource, MavenPomXmlDescriptor> {

    private MavenXpp3Reader mavenXpp3Reader;

    @Override
    public void initialize() {
        mavenXpp3Reader = new MavenXpp3Reader();
    }

    @Override
    public Class<? extends FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public Class<? extends MavenPomXmlDescriptor> getDescriptorType() {
        return MavenPomXmlDescriptor.class;
    }

    /** {@inheritDoc} */
    @Override
    public MavenPomXmlDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        Model model;
        try (InputStream stream = item.createStream()) {
            model = mavenXpp3Reader.read(stream);
        } catch (XmlPullParserException e) {
            throw new IOException("Cannot read POM descriptor.", e);
        }
        MavenPomXmlDescriptor pomXmlDescriptor = scanner.scan(model, path, scope);
        scanner.getContext().push(XmlFileDescriptor.class, pomXmlDescriptor);
        try {
            scanner.scan(item, path, XmlScope.DOCUMENT);
        } finally {
            scanner.getContext().pop(XmlFileDescriptor.class);
        }
        return pomXmlDescriptor;
    }

}
