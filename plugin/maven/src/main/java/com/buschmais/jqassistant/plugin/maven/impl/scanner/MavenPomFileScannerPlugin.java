package com.buschmais.jqassistant.plugin.maven.impl.scanner;

import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.maven.api.model.MavenPomDescriptor;
import com.buschmais.jqassistant.plugin.maven.api.model.MavenPomXmlDescriptor;
import com.buschmais.jqassistant.plugin.maven.api.scanner.PomModelBuilder;
import com.buschmais.jqassistant.plugin.maven.api.scanner.RawModelBuilder;
import com.buschmais.jqassistant.plugin.xml.api.scanner.AbstractXmlFileScannerPlugin;
import com.buschmais.jqassistant.plugin.xml.api.scanner.XMLFileFilter;

import org.apache.maven.api.model.Model;

/**
 * Scans pom.xml files.
 *
 * @author ronald.kunzmann@buschmais.com
 */
public class MavenPomFileScannerPlugin extends AbstractXmlFileScannerPlugin<MavenPomXmlDescriptor> {

    private final RawModelBuilder rawModelBuilder = new RawModelBuilder();

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        boolean hasXMLExtension = path.toLowerCase()
            .endsWith(".xml");
        boolean isPomXML = path.toLowerCase()
            .endsWith("pom.xml");
        boolean hasPomExtension = path.toLowerCase()
            .endsWith(".pom");
        boolean identifiedByExtension = isPomXML || hasPomExtension;

        boolean isMavenPOM;
        if (!identifiedByExtension && hasXMLExtension) {
            // Maven 4.1.0 uses namespace http://maven.apache.org/POM/4.1.0 (root element remains "project")
            isMavenPOM = XMLFileFilter.rootElementMatches(item, path,
                rootElement -> "project".equals(rootElement.getLocalPart()) && rootElement.getNamespaceURI()
                    .startsWith("http://maven.apache.org/POM/"));
        } else {
            isMavenPOM = identifiedByExtension;
        }

        return isMavenPOM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MavenPomXmlDescriptor scan(FileResource item, MavenPomXmlDescriptor mavenPomXmlDescriptor, String path, Scope scope, Scanner scanner)
        throws IOException {
        Model model = getModel(item, path, scanner);
        if (model != null) {
            scanner.getContext()
                .push(MavenPomDescriptor.class, mavenPomXmlDescriptor);
            try {
                MavenPomXmlDescriptor result = scanner.scan(model, path, scope);
                if (result != null) {
                    result.setValid(true);
                    return result;
                } else {
                    mavenPomXmlDescriptor.setValid(false);
                    return mavenPomXmlDescriptor;
                }
            } finally {
                scanner.getContext()
                    .pop(MavenPomDescriptor.class);
            }
        } else {
            mavenPomXmlDescriptor.setValid(false);
            return mavenPomXmlDescriptor;
        }
    }

    /**
     * Build the POM model from the given file resource (i.e. a pom.xml).
     *
     * @param item
     *     The file resource.
     * @param path
     *     the resource path.
     * @param scanner
     *     The scanner.
     * @return The model.
     * @throws IOException
     *     If the model cannot be read.
     */
    private Model getModel(FileResource item, String path, Scanner scanner) throws IOException {
        try (InputStream stream = item.createStream()) {
            PomModelBuilder pomModelBuilder = scanner.getContext()
                .peekOrDefault(PomModelBuilder.class, null);
            if (pomModelBuilder != null) {
                return pomModelBuilder.getModel(stream, path);
            }
            return rawModelBuilder.getModel(stream, path);
        }
    }

}
