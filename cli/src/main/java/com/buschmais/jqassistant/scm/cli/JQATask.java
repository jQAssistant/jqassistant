package com.buschmais.jqassistant.scm.cli;

import java.util.Map;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface JQATask extends Runnable, OptionsProvider {

    void initialize(Map<String, Object> properties);

    String getName();
}
