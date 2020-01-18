package com.buschmais.jqassistant.plugin.xml.api.scanner;

import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Utility class for unmarshalling file resources.
 *
 * @param <X>
 *            The JAXB type of the root element.
 */
public class FileResourceJAXBUnmarshaller<X> extends com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller<X> {

    public FileResourceJAXBUnmarshaller(Class<X> rootElementType) {
        super(rootElementType);
    }

    public X unmarshal(FileResource item) throws IOException {
        try (InputStream stream = item.createStream()) {
            return unmarshal(stream);
        }
    }

}
