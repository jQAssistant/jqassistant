package com.buschmais.jqassistant.core.rule.api.source;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines a source containing rules.
 *
 * @author mh
 * @since 12.10.14
 */
public abstract class RuleSource {

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
