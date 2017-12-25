package com.buschmais.jqassistant.plugin.junit.test.set.junit5;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class DisabledTestWithoutMessage {
    @Disabled
    @Test
    void iHaveNoMessage() {
    }
}
