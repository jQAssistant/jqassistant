package com.buschmais.jqassistant.core.shared.io;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Provides functionality to load test resources.
 */
public class ClasspathResource {

    public static File getFile(String resource) {
        return getFile(ClasspathResource.class, resource);
    }

    public static File getFile(Class<?> type, String resource) {
        URL url = type.getResource(resource);
        try {
            return new File(URLDecoder.decode(url.getFile(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Cannot decode URL " + url, e);
        }
    }

}
