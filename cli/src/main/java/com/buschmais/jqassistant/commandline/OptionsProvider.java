package com.buschmais.jqassistant.commandline;

import org.apache.commons.cli.Option;

import java.util.List;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface OptionsProvider {
    List<Option> getOptions();
}
