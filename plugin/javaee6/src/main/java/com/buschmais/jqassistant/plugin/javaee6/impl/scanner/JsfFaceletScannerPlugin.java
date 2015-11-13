package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.javaee6.api.model.JsfFaceletDescriptor;

/**
 * Scans JSF template files and sets relationships between them.
 *
 * @author peter.herklotz@buschmais.com
 */
@Requires(FileDescriptor.class)
public class JsfFaceletScannerPlugin extends AbstractScannerPlugin<FileResource, JsfFaceletDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsfFaceletScannerPlugin.class);

    private static final String DEFAULT_FILE_PATTERN = ".*\\.jspx";
    private static final String PROPERTY_NAME_FILE_PATTERN = "facelet.file.pattern";

    private DocumentBuilder documentBuilder;
    private Pattern filePattern;
    private XPath xPath;

    /** {@inheritDoc} */
    @Override
    public void initialize() {
        // initialize xpath
        XPathFactory xPathfactory = XPathFactory.newInstance();
        xPath = xPathfactory.newXPath();
        xPath.setNamespaceContext(new NamespaceContext() {

            @Override
            @SuppressWarnings("rawtypes")
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix != null) {
                    if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
                        return "http://www.w3.org/1999/xhtml";
                    } else if (prefix.equals("ui")) {
                        return "http://java.sun.com/jsf/facelets";
                    } else if (prefix.equals("h")) {
                        return "http://java.sun.com/jsf/html";
                    } else if (prefix.equals("f")) {
                        return "http://java.sun.com/jsf/core";
                    }
                }
                return XMLConstants.NULL_NS_URI;
            }
        });

        // initialize DocumentBuilder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        try {
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            LOGGER.warn("Cannot set features of document builder factory.", e);
        }
        try {
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Cannot create document builder.", e);
        }

    } // end method initialize

    @Override
    public void configure() {
        if (getProperties().containsKey(PROPERTY_NAME_FILE_PATTERN)) {
            filePattern = Pattern.compile(getProperties().get(PROPERTY_NAME_FILE_PATTERN).toString());
        } else {
            filePattern = Pattern.compile(DEFAULT_FILE_PATTERN);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return filePattern.matcher(path).find();
    }

    /** {@inheritDoc} */
    @Override
    public JsfFaceletDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ScannerContext context = scanner.getContext();
        Store store = context.getStore();
        FileDescriptor fileDescriptor = context.peek(FileDescriptor.class);
        JsfFaceletDescriptor currentDescriptor = store.addDescriptorType(fileDescriptor, JsfFaceletDescriptor.class);

        try {
            // read the xml file
            Document doc = getDocument(item);
            // find includes
            NodeList nodeList = (NodeList) getXPath().evaluate("//ui:include", doc, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String includeSrc = node.getAttributes().getNamedItem("src").getNodeValue();
                JsfFaceletDescriptor includedJsf = findJsfTemplateDescriptor(path, includeSrc, context);

                if (includedJsf != null) {
                    currentDescriptor.getIncludes().add(includedJsf);
                }
            }

            // find template relationship
            String templateSrc = (String) getXPath().evaluate("//ui:composition/@template", doc, XPathConstants.STRING);

            if (StringUtils.isNotBlank(templateSrc)) {
                currentDescriptor.setTemplate(findJsfTemplateDescriptor(path, templateSrc, context));
            }
        } catch (XPathExpressionException | SAXException e) {
            throw new IOException(e);
        }

        return currentDescriptor;
    } // end method scan

    /**
     * Try to find an existing {@link JsfFaceletDescriptor} with the given
     * parameters.
     *
     * @param templateFqn
     *            full qualified name of the including file
     * @param path
     *            the found path to the included file
     * @param context
     *            The scanner context
     * @return an existing {@link JsfFaceletDescriptor} or a new one if no
     *         descriptor exists.
     */
    private JsfFaceletDescriptor findJsfTemplateDescriptor(String templateFqn, String path, ScannerContext context) {
        String includedFile = absolutifyFilePath(path, templateFqn);
        return getJsfTemplateDescriptor(includedFile, context);
    }

    /**
     * Normalize file paths like the jqassistant core (e.g. replace backslashs,
     * add leading slash).
     *
     * @param path
     *            the path to normalize
     *
     * @return the normalized path
     */
    private String normalizeFilePath(final String path) {
        String normalizedPath = path.replace('\\', '/');

        if (!normalizedPath.startsWith("/")) {
            normalizedPath = "/" + normalizedPath;
        }

        return normalizedPath;
    }

    /**
     * <p>
     * Try to make an absolute path from a relative path and a reference path.
     * </p>
     *
     * <p>
     * <b>Example:</b><br>
     * <i>path</i> - ../../a/b.jspx<br>
     * <i>referencePath</i> - c/d/e/f.jspx<br>
     * <b>Result: c/a/b.jspx</b>
     * </p>
     *
     * @param path
     *            the relative path
     * @param referencePath
     *            the reference file path (should be absolute)
     *
     * @return a path absolute path in reference to the second path.
     */
    private String absolutifyFilePath(final String path, final String referencePath) {
        // can't handle EL-expressions
        if (isElExpression(path)) {
            return path;
        }

        String normalizedPath;
        if (!path.startsWith("/")) {
            Path reference = Paths.get(referencePath);
            normalizedPath = reference.getParent().resolve(path).normalize().toString();
        } else {
            normalizedPath = Paths.get(path).normalize().toString();
        }

        return normalizeFilePath(normalizedPath);
    }

    /**
     * Parses the xml file with a {@link DocumentBuilder}.
     *
     * @param resource
     *            the file to read
     *
     * @return a {@link Document}
     *
     * @throws SAXException
     *             see {@link DocumentBuilder#parse(java.io.File)}
     * @throws IOException
     *             see {@link DocumentBuilder#parse(java.io.File)}
     */
    private Document getDocument(FileResource resource) throws SAXException, IOException {
        return documentBuilder.parse(resource.getFile());
    }

    /**
     * Tries to find a {@link JsfFaceletDescriptor} in the store with the given
     * fullFilePath. Creates a new one if no descriptor could be found.
     *
     * @param fullFilePath
     *            the file path of the wanted file
     * @param context
     *            The scanner context.
     * @return a {@link JsfFaceletDescriptor} representing the wanted file
     */
    private JsfFaceletDescriptor getJsfTemplateDescriptor(String fullFilePath, ScannerContext context) {
        // can't handle EL-expressions
        if (isElExpression(fullFilePath)) {
            return null;
        }
        return context.peek(FileResolver.class).require(fullFilePath, JsfFaceletDescriptor.class, context);
    }

    /**
     * If the argument starts with '#{' or '${' it is an EL-expression.
     *
     * @param str
     *            input string
     *
     * @return <code>true</code> if the argument starts with '#{' or '${',
     *         otherwise <code>
     *          false</code>
     */
    private boolean isElExpression(String str) {
        return str.startsWith("#{") || str.startsWith("${");
    }

    /**
     * Lazy getter for {@link XPath} object.
     *
     * @return the xpath object.
     */
    private XPath getXPath() {
        return xPath;
    }

}
