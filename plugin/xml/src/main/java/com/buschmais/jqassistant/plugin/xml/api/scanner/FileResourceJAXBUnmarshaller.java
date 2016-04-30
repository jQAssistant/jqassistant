package com.buschmais.jqassistant.plugin.xml.api.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Utility class for unmarshalling file resources.
 * <p>
 * A constructor is provided that takes namespace mappings which may be used to
 * unmarshal documents using an older, i.e. compatible schema version (e.g. for
 * reading a persistence.xml 2.0 descriptor using a 2.1 JAXBContext).
 * <p>
 *
 * @param <X> The JAXB type of the root element.
 */
public class FileResourceJAXBUnmarshaller<X> extends com.buschmais.jqassistant.core.shared.xml.JAXBUnmarshaller<X> {

    public FileResourceJAXBUnmarshaller(Class<X> rootElementType) {
        super(rootElementType);
    }

    public FileResourceJAXBUnmarshaller(Class<X> rootElementType, Map<String, String> namespaceMapping) {
        super(rootElementType, namespaceMapping);
    }

    public X unmarshal(FileResource item) throws IOException {
        try (InputStream stream = item.createStream()) {
            return unmarshal(stream);
        }
    }

}
