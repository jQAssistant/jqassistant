package com.buschmais.jqassistant.core.shared.xml;

import java.io.IOException;

import javax.xml.validation.Schema;

import org.jqassistant.schema.test.v1.JqassistantTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JAXBHelperTest {

    private static final String RULES_SCHEMA_LOCATION = "/META-INF/schema/jqassistant-test-v1.0.xsd";
    private static final String NAMESPACE = "http://schema.jqassistant.org/test/v1.0";

    private static final Schema SCHEMA = XmlHelper.getSchema(RULES_SCHEMA_LOCATION);

    private JAXBHelper<JqassistantTest> jaxbHelper = new JAXBHelper<>(JqassistantTest.class, SCHEMA, NAMESPACE);

    @Test
    void valid() throws IOException {
        JqassistantTest jqassistantTest = jaxbHelper.unmarshal(JAXBHelperTest.class.getResource("/jqassistant-test/jqassistant-test-valid.xml"));

        assertThat(jqassistantTest).isNotNull();
        assertThat(jqassistantTest.getId()).isEqualTo("valid");
    }

    @Test
    void additionalElementAndAttribute() throws IOException {
        JqassistantTest jqassistantTest = jaxbHelper.unmarshal(
                JAXBHelperTest.class.getResource("/jqassistant-test/jqassistant-additional-element-and-attribute.xml"));

        assertThat(jqassistantTest).isNotNull();
        assertThat(jqassistantTest.getId()).isEqualTo("additional-element-and-attribute");
    }
}