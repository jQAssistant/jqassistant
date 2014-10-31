package com.buschmais.jqassistant.core.analysis.api.rule.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

/**
 * @author mh
 * @since 12.10.14
 */
public abstract class RuleSource {

    private final Type type;

    protected RuleSource() {
        type = selectType();
    }

    private Type selectType() {
        String path = getId();
        for (Type type : Type.values()) {
            if (type.matches(path))
                return type;
        }
        throw new IllegalArgumentException("No matching type found for " + path + " available types " + Arrays.toString(Type.values()));
    }

    public boolean isType(Type type) {
        return this.type == type;
    }

    public enum Type {
        AsciiDoc("adoc"), XML("xml");
        String ext;

        Type(String ext) {
            this.ext = ext;
        }

        public boolean matches(File file) {
            return matches(file.getName());
        }

        boolean matches(URI uri) {
            return matches(uri.getPath());
        }

        boolean matches(String path) {
            return path.toLowerCase().endsWith("." + ext);
        }
    }

    public abstract String getId();

    public abstract InputStream getInputStream() throws IOException;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RuleSource that = (RuleSource) o;
        return type == that.type && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return 31 * getId().hashCode() + type.hashCode();
    }
}
