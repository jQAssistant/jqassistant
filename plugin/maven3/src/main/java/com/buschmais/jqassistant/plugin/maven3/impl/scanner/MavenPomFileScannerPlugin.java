package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.scanner.PomModelBuilder;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XMLFileFilter;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XmlScope;

/**
 * Scans pom.xml files.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
public class MavenPomFileScannerPlugin extends AbstractScannerPlugin<FileResource, MavenPomXmlDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenPomFileScannerPlugin.class);

    private MavenXpp3Reader mavenXpp3Reader;

    @Override
    public void initialize() {
        mavenXpp3Reader = new MavenXpp3Reader();
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        boolean isMavenPOM = false;
        boolean hasXMLExtension = path.toLowerCase().endsWith(".xml");
        boolean isPomXML = path.toLowerCase().endsWith("pom.xml");
        boolean hasPomExtension = path.toLowerCase().endsWith(".pom");
        boolean identifiedByExtension = !XmlScope.DOCUMENT.equals(scope) && (isPomXML || hasPomExtension);

        if (!identifiedByExtension && hasXMLExtension) {
            isMavenPOM = XMLFileFilter.rootElementMatches(item, path, "project");
        } else {
            isMavenPOM = identifiedByExtension;
        }

        return isMavenPOM;
    }

    /** {@inheritDoc} */
    @Override
    public MavenPomXmlDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        XmlFileDescriptor xmlFileDescriptor = scanner.scan(item, path, XmlScope.DOCUMENT);
        MavenPomXmlDescriptor mavenPomXmlDescriptor = scanner.getContext().getStore().addDescriptorType(xmlFileDescriptor, MavenPomXmlDescriptor.class);
        Model model = getModel(item, scanner);
        if (model != null) {
            mavenPomXmlDescriptor.setValid(true);
            scanner.getContext().push(MavenPomDescriptor.class, mavenPomXmlDescriptor);
            try {
                scanner.scan(model, path, scope);
            } finally {
                scanner.getContext().pop(MavenPomDescriptor.class);
            }
        } else {
            mavenPomXmlDescriptor.setValid(false);
        }
        return mavenPomXmlDescriptor;
    }

    /**
     * Build the POM model from the given file resource (i.e. a pom.xml).
     * 
     * @param item
     *            The file resource.
     * @param scanner
     *            The scanner.
     * @return The model.
     * @throws IOException
     *             If the model cannot be read.
     */
    private Model getModel(FileResource item, Scanner scanner) throws IOException {
        PomModelBuilder pomModelBuilder = scanner.getContext().peekOrDefault(PomModelBuilder.class, null);
        if (pomModelBuilder != null) {
            return pomModelBuilder.getModel(item.getFile());
        }
        Model model = null;
        try (InputStream stream = item.createStream()) {
            model = mavenXpp3Reader.read(stream);
        } catch (XmlPullParserException e) {
            LOGGER.warn("Cannot read POM descriptor.", e);
        }
        return model;
    }

}
