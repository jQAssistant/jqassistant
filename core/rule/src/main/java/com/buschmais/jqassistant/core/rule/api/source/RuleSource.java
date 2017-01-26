package com.buschmais.jqassistant.core.rule.api.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author mh
 * @since 12.10.14
 */
public abstract class RuleSource {

    public boolean isType(Type type) {
        Type thisType = getType();
        if (thisType == null) {
            throw new IllegalArgumentException("No matching type found for " + getId() + " available types " + Arrays.toString(Type.values()));
        }
        return type.equals(thisType);
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

        boolean matches(String path) {
            return path.toLowerCase().endsWith("." + ext);
        }
    }

    protected Type selectTypeById() {
        String path = getId();
        for (Type type : Type.values()) {
            if (type.matches(path)) {
                return type;
            }
        }
        return null;
    }

    protected abstract Type getType();

    public abstract String getId();

    public abstract InputStream getInputStream() throws IOException;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RuleSource that = (RuleSource) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return getId();
    }
}
