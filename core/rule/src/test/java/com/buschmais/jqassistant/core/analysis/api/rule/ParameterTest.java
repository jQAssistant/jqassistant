package com.buschmais.jqassistant.core.analysis.api.rule;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParameterTest {

    @Test
    public void charParameter() throws RuleException {
        assertThat(Parameter.Type.CHAR.parse("4")).isEqualTo('4');
    }

    @Test
    public void byteParameter() throws RuleException {
        assertThat(Parameter.Type.BYTE.parse("42")).isEqualTo((byte)42);
    }

    @Test
    public void intParameter() throws RuleException {
        assertThat(Parameter.Type.INT.parse("42")).isEqualTo(42);
    }

    @Test
    public void shortParameter() throws RuleException {
        assertThat(Parameter.Type.SHORT.parse("42")).isEqualTo((short)42);
    }

    @Test
    public void longParameter() throws RuleException {
        assertThat(Parameter.Type.LONG.parse("42")).isEqualTo(42L);
    }

    @Test
    public void floatParameter() throws RuleException {
        assertThat(Parameter.Type.FLOAT.parse("42.42")).isEqualTo(42.42F);
    }

    @Test
    public void doubleParameter() throws RuleException {
        assertThat(Parameter.Type.DOUBLE.parse("42.42")).isEqualTo(42.42D);
    }

    @Test
    public void booleanParameter() throws RuleException {
        assertThat(Parameter.Type.BOOLEAN.parse("TRUE")).isEqualTo(true);
        assertThat(Parameter.Type.BOOLEAN.parse("true")).isEqualTo(true);
        assertThat(Parameter.Type.BOOLEAN.parse("FALSE")).isEqualTo(false);
        assertThat(Parameter.Type.BOOLEAN.parse("false")).isEqualTo(false);
    }

    @Test
    public void stringParameter() throws RuleException {
        assertThat(Parameter.Type.STRING.parse("fortyTwo")).isInstanceOf(String.class)
                                                           .isEqualTo("fortyTwo");
    }

    @Test
    public void nullValues() throws RuleException {
        for (Parameter.Type type : Parameter.Type.values()) {
            assertThat(type.parse(null)).isNull();
        }
    }

}
