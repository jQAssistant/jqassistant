package com.buschmais.jqassistant.core.report.model;

import static com.buschmais.jqassistant.core.report.model.TestLanguage.TestLanguageElement.TestElement;

import com.buschmais.xo.api.CompositeObject;

/**
 * A test descriptor.
 */
@TestLanguage(TestElement)
public interface TestDescriptorWithLanguageElement extends CompositeObject {

    String getValue();
}
