package com.buschmais.jqassistant.core.rule.api.source;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Defines a source containing rules.
 *
 * @author mh
 * @since 12.10.14
 */
public abstract class RuleSource {

    /**
     * Return a unique identifier of this {@link RuleSource}.
     * 
     * @return The identifier.
     */
    public abstract String getId();

    /**
     * Return the {@link URL} of the {@link RuleSource}.
     * 
     * @return The {@link URL}.
     * @throws IOException
     *             If the {@link URL} cannot be determined.
     */
    public abstract URL getURL() throws IOException;

    /**
     * Open an {@link InputStream} providing the content of the {@link RuleSource}.
     * 
     * @return The {@link InputStream}.
     * @throws IOException
     *             If the {@link InputStream} cannot be opened.
     */
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
