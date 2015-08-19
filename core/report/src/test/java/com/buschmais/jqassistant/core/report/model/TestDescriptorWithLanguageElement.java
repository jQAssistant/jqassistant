package com.buschmais.jqassistant.core.report.model;

import static com.buschmais.jqassistant.core.report.model.TestLanguage.TestLanguageElement.TestElement;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * A test descriptor.
 */
@TestLanguage(TestElement)
public interface TestDescriptorWithLanguageElement extends Descriptor {

    String getValue();
}
