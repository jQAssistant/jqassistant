package com.buschmais.jqassistant.core.shared.xml;

import javax.xml.validation.Schema;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

/**
 * Utility class for unmarshalling XML documents using JAXB.
 * <p>
 * A constructor is provided that takes a target namespace for to be used for
 * unmarshalling (e.g. for reading a persistence.xml 2.0 descriptor using a 2.1
 * JAXBContext).
 *
 * @param <X>
 *     The JAXB type of the root element.
 * @deprecated Use {@link JAXBHelper}.
 */
@ToBeRemovedInVersion(major = 2, minor = 5)
@Deprecated(forRemoval = true)
public class JAXBUnmarshaller<X> extends JAXBHelper<X> {

    /**
     * Constructor.
     *
     * @param rootElementType
     *     The expected root element type.
     */
    public JAXBUnmarshaller(Class<X> rootElementType) {
        super(rootElementType, null, null);
    }

    /**
     * Constructor.
     *
     * @param rootElementType
     *     The expected root element type.
     * @param targetNamespace
     *     The target namespace to use for unmarshalling.
     */
    public JAXBUnmarshaller(Class<X> rootElementType, String targetNamespace) {
        super(rootElementType, null, targetNamespace);
    }

    /**
     * Constructor.
     *
     * @param rootElementType
     *     The expected root element type.
     * @param schema
     *     The optional schema for validation.
     * @param targetNamespace
     *     The target namespace to use for unmarshalling.
     */
    public JAXBUnmarshaller(Class<X> rootElementType, Schema schema, String targetNamespace) {
        super(rootElementType, schema, targetNamespace);
    }
}
