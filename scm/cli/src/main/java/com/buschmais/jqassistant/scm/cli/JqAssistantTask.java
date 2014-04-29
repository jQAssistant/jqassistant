package com.buschmais.jqassistant.scm.cli;

/**
 * @author jn4, Kontext E GmbH, 17.02.14
 */
public interface JqAssistantTask extends Runnable, OptionsProvider {
   String getName();
}
