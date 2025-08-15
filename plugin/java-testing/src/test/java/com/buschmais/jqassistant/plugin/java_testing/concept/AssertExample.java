package com.buschmais.jqassistant.plugin.java_testing.concept;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests;
import org.mockito.BDDMockito;
import org.xmlunit.assertj.XmlAssert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Example class used by integration tests.
 */
public class AssertExample {

    void assertjAssertExampleMethod() {
        Assertions.assertThat(true).isTrue();
    }

    void xmlAssertExampleMethod() {
        XmlAssert.assertThat("<nop/>").and("<nop />").normalizeWhitespace().areSimilar();
    }


    void mockitoVerifyExampleMethod() {
        verify(mock(Object.class).equals(any(Object.class)));
    }

    void bddMockitoThenShouldExampleMethod() {
        BDDMockito.then(mock(Object.class)).shouldHaveNoInteractions();
    }

    void camundaBpmnAssertExampleMethod() {
        ProcessDefinition processDefinition = mock(ProcessDefinition.class);
        BpmnAwareTests.assertThat(processDefinition).hasActiveInstances(0);
    }

}
