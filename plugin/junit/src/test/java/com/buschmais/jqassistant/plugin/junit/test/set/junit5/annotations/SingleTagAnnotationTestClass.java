package com.buschmais.jqassistant.plugin.junit.test.set.junit5.annotations;

import org.junit.jupiter.api.Test;

public class SingleTagAnnotationTestClass {

    @SingleTagAnnotation
    @Test
    public int getInt() {
        return 0;
    }
}
