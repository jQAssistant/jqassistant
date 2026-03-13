package com.buschmais.jqassistant.core.rule.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParameterTest {

    @Test
    void charParameter() throws RuleException {
        assertThat(Parameter.Type.CHAR.parse("4")).isEqualTo('4');
    }

    @Test
    void byteParameter() throws RuleException {
        assertThat(Parameter.Type.BYTE.parse("42")).isEqualTo((byte)42);
    }

    @Test
    void intParameter() throws RuleException {
        assertThat(Parameter.Type.INT.parse("42")).isEqualTo(42);
    }

    @Test
    void shortParameter() throws RuleException {
        assertThat(Parameter.Type.SHORT.parse("42")).isEqualTo((short)42);
    }

    @Test
    void longParameter() throws RuleException {
        assertThat(Parameter.Type.LONG.parse("42")).isEqualTo(42L);
    }

    @Test
    void floatParameter() throws RuleException {
        assertThat(Parameter.Type.FLOAT.parse("42.42")).isEqualTo(42.42F);
    }

    @Test
    void doubleParameter() throws RuleException {
        assertThat(Parameter.Type.DOUBLE.parse("42.42")).isEqualTo(42.42D);
    }

    @Test
    void booleanParameter() throws RuleException {
        assertThat(Parameter.Type.BOOLEAN.parse("TRUE")).isEqualTo(true);
        assertThat(Parameter.Type.BOOLEAN.parse("true")).isEqualTo(true);
        assertThat(Parameter.Type.BOOLEAN.parse("FALSE")).isEqualTo(false);
        assertThat(Parameter.Type.BOOLEAN.parse("false")).isEqualTo(false);
    }

    @Test
    void stringParameter() throws RuleException {
        assertThat(Parameter.Type.STRING.parse("fortyTwo")).isInstanceOf(String.class)
                                                           .isEqualTo("fortyTwo");
    }

    @Test
    void nullValues() throws RuleException {
        for (Parameter.Type type : Parameter.Type.values()) {
            assertThat(type.parse(null)).isNull();
        }
    }

}
