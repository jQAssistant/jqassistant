package com.buschmais.jqassistant.core.analysis.api.rule;

import java.io.IOException;
import java.io.InputStream;

/**
 * Defines the source for reading rules.
 */
public interface RuleSource {

    /**
     * The name of the source, e.g. file name, URL, etc.
     * 
     * @return The name.
     */
    String getId();

    /**
     * Open a stream to read the rule.
     * 
     * @return The stream.
     */
    InputStream getInputStream() throws IOException;

}
