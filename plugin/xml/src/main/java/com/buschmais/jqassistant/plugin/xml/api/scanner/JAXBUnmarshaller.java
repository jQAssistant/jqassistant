package com.buschmais.jqassistant.plugin.xml.api.scanner;

import java.util.Map;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

/**
 * Utility class for unmarshalling file resources.
 * <p>
 * A constructor is provided that takes namespace mappings which may be used to
 * unmarshal documents using an older, i.e. compatible schema version (e.g. for
 * reading a persistence.xml 2.0 descriptor using a 2.1 JAXBContext).
 * <p>
 * Note: This class has been deprecated and replaced by {@link FileResourceJAXBUnmarshaller}.
 *
 * @param <X> The JAXB type of the root element.
 */
@Deprecated
@ToBeRemovedInVersion(major = 2, minor = 0)
public class JAXBUnmarshaller<X> extends FileResourceJAXBUnmarshaller<X> {

    public JAXBUnmarshaller(Class<X> rootElementType) {
        super(rootElementType);
    }

    public JAXBUnmarshaller(Class<X> rootElementType, Map<String, String> namespaceMapping) {
        super(rootElementType, namespaceMapping);
    }
}
