package com.buschmais.jqassistant.core.analysis.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

public class SeverityTest {

    @Test
    public void getSeverityFromName() throws RuleException {
        for (Severity severity : Severity.values()) {
            String name = severity.getValue();
            assertThat(Severity.fromValue(name), is(severity));
        }
    }

    @Test(expected = RuleException.class)
    public void unknownSeverity() throws RuleException {
        Severity.fromValue("foo");
    }

    @Test
    public void noSeverity() throws RuleException {
        assertThat(Severity.fromValue(null), nullValue());
    }
}
