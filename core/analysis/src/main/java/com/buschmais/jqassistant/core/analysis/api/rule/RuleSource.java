package com.buschmais.jqassistant.core.analysis.api.rule;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

/**
 * @author mh
 * @since 12.10.14
 */
public class RuleSource {
    private final URL url;
    private final Type type;

    public RuleSource(URL url, Type type) {
        this.url = url;
        this.type = type;
    }

    public RuleSource(URL url) {
        this.url = url;
        this.type = selectType(url);
    }

    public RuleSource(File file) {
        try {
            this.url = file.toURI().toURL();
            this.type = selectType(this.url);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Malformed URL for file "+file,e);
        }
    }

    private Type selectType(URL url) {
        String path = url.getPath();
        for (Type type : Type.values()) {
            if (type.matches(path)) return type;
        }
        throw new IllegalArgumentException("No matching type found for "+url+" available types "+ Arrays.toString(Type.values()));
    }

    public boolean isType(Type type) {
        return this.type == type;
    }

    public URL getUrl() {
        return url;
    }

    public String getId() {
        return url.toExternalForm();
    }

    public enum Type {  AsciiDoc("adoc"), XML("xml"); String ext;

        Type(String ext) {
            this.ext = ext;
        }
        public boolean matches(File file) { return matches(file.getName()); }
        boolean matches(URI uri) { return matches(uri.getPath()); }
        boolean matches(String path) { return path.toLowerCase().endsWith("." + ext); }
    }

    public Source toSource() {
        if (type == Type.XML)
            return new StreamSource(getInputStream(),getId());
        throw new IllegalStateException("Creating Sources not supported for non XML-URLs "+url);
    }

    public InputStream getInputStream() {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new IllegalStateException("Could not create StreamSource for URL "+url);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleSource that = (RuleSource) o;
        return type == that.type && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return 31 * url.hashCode() + type.hashCode();
    }
}
