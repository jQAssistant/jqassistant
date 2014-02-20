package com.buschmais.jqassistant.core.analysis.impl;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.regex.Pattern;

/**
 * A [@link XMLStreamWriter} which transparently writes CDATA blocks if special characters are detected.
 *
 * @see <a href="http://blog.mi-ernst.de/2012/05/04/jaxb-and-cdata-sections/"/>here</a>.
 */
public class CDataXMLStreamWriter extends AbstractStreamWriterDelegate {

    private static final Pattern XML_CHARS = Pattern.compile("[&<>]");

    public CDataXMLStreamWriter(XMLStreamWriter delegate) {
        super(delegate);
    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        boolean useCData = XML_CHARS.matcher(text).find();
        if (useCData) {
            super.writeCData(text);
        } else {
            super.writeCharacters(text);
        }
    }
}
