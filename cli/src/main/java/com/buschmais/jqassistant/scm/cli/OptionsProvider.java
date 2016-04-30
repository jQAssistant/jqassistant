package com.buschmais.jqassistant.scm.cli;

import java.util.List;

import org.apache.commons.cli.Option;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface OptionsProvider {
    List<Option> getOptions();
}
