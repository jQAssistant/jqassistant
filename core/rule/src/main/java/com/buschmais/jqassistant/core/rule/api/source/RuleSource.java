package com.buschmais.jqassistant.core.rule.api.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

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
     * Return the relative path of the rule source (e.g. relative to the jqassistant
     * rule directory or within the classpath).
     *
     * @return The relative path.
     */
    public abstract String getRelativePath();

    public abstract Optional<File> getDirectory();

    /**
     * Open an {@link InputStream} providing the content of the {@link RuleSource}.
     *
     * @return The {@link InputStream}.
     * @throws IOException
     *             If the {@link InputStream} cannot be opened.
     */
    public abstract InputStream getInputStream() throws IOException;

    @Override
    public final boolean equals(Object o) {
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
    public final int hashCode() {
        return getId().hashCode();
    }

    @Override
    public final String toString() {
        return getId();
    }
}
