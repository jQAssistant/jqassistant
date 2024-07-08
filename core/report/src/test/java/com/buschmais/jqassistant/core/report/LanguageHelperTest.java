package com.buschmais.jqassistant.core.report;

import com.buschmais.jqassistant.core.report.api.LanguageHelper;
import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.core.report.api.model.LanguageElement;
import com.buschmais.jqassistant.core.report.model.DerivedTestDescriptor;
import com.buschmais.jqassistant.core.report.model.DerivedTestDescriptorWithLanguageElement;
import com.buschmais.jqassistant.core.report.model.TestDescriptorWithLanguageElement;
import com.buschmais.jqassistant.core.report.model.TestLanguage.TestLanguageElement;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class LanguageHelperTest {

    @Test
    public void resolveNameFromDescriptorWithLanguageElement() {
        TestDescriptorWithLanguageElement testDescriptor = Mockito.mock(TestDescriptorWithLanguageElement.class);
        resolveName(testDescriptor, TestLanguageElement.TestElement);
    }

    @Test
    void resolveNameFromDerivedDescriptor() {
        DerivedTestDescriptor derivedTestDescriptor = Mockito.mock(DerivedTestDescriptor.class);
        resolveName(derivedTestDescriptor, TestLanguageElement.TestElement);
    }

    @Test
    void resolveNameFromDerivedDescriptorWithLanguageElement() {
        DerivedTestDescriptorWithLanguageElement derivedTestDescriptor = Mockito.mock(DerivedTestDescriptorWithLanguageElement.class);
        resolveName(derivedTestDescriptor, TestLanguageElement.DerivedTestElement);
    }

    private void resolveName(TestDescriptorWithLanguageElement testDescriptor, LanguageElement expectedLanguageElement) {
        when(testDescriptor.getValue()).thenReturn("value");
        LanguageElement languageElement = LanguageHelper.getLanguageElement(testDescriptor);
        assertThat(languageElement).isNotNull();
        assertThat(languageElement).isEqualTo(expectedLanguageElement);
        SourceProvider<TestDescriptorWithLanguageElement> sourceProvider = languageElement.getSourceProvider();
        String name = sourceProvider.getName(testDescriptor);
        assertThat(name).isEqualTo("value");
    }
}
