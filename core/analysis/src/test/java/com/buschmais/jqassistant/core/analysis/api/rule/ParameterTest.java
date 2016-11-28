package com.buschmais.jqassistant.core.analysis.api.rule;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class ParameterTest {

    @Test
    public void intParameter() {
        assertThat(Parameter.Type.INT.parse("42"), CoreMatchers.<Object> equalTo(42));
    }

    @Test
    public void shortParameter() {
        assertThat(Parameter.Type.SHORT.parse("42"), CoreMatchers.<Object> equalTo((short) 42));
    }

    @Test
    public void longParameter() {
        assertThat(Parameter.Type.LONG.parse("42"), CoreMatchers.<Object> equalTo((long) 42));
    }

    @Test
    public void floatParameter() {
        assertThat(Parameter.Type.FLOAT.parse("42.42"), CoreMatchers.<Object> equalTo((float) 42.42));
    }

    @Test
    public void doubleParameter() {
        assertThat(Parameter.Type.DOUBLE.parse("42.42"), CoreMatchers.<Object> equalTo(42.42));
    }

    @Test
    public void booleanParameter() {
        assertThat(Parameter.Type.BOOLEAN.parse("TRUE"), CoreMatchers.<Object> equalTo(true));
        assertThat(Parameter.Type.BOOLEAN.parse("true"), CoreMatchers.<Object> equalTo(true));
        assertThat(Parameter.Type.BOOLEAN.parse("FALSE"), CoreMatchers.<Object> equalTo(false));
        assertThat(Parameter.Type.BOOLEAN.parse("false"), CoreMatchers.<Object> equalTo(false));
    }

    @Test
    public void stringParameter() {
        assertThat(Parameter.Type.STRING.parse("fortyTwo"), CoreMatchers.<Object> equalTo("fortyTwo"));
    }

    @Test
    public void nullValues() {
        for (Parameter.Type type : Parameter.Type.values()) {
            assertThat(type.parse(null), nullValue());
        }
    }

}
