package com.buschmais.jqassistant.scm.cli;

import org.apache.commons.cli.CommandLine;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface OptionsConsumer {
    void withOptions(final CommandLine options);
}
