package com.buschmais.jqassistant.core.report.model;

import com.buschmais.xo.api.CompositeObject;

import static com.buschmais.jqassistant.core.report.model.TestLanguage.TestLanguageElement.TestElement;

/**
 * A test descriptor.
 */
@TestLanguage(TestElement)
public interface TestDescriptorWithLanguageElement extends CompositeObject {

    String getValue();
}
