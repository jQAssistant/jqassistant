package com.buschmais.jqassistant.core.report;

import static com.buschmais.jqassistant.core.report.TestLanguage.TestLanguageElement.TestElement;

import com.buschmais.jqassistant.core.store.api.type.Descriptor;

/**
 * A test descriptor.
 */
@TestLanguage(TestElement)
public interface TestDescriptor extends Descriptor {

    String getValue();
}
