package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class MethodDataFlowVerifierTest {

    private static final Type INTERFACE = Type.getType(TestInterface.class);
    private static final Type EXCEPTION = Type.getType(TestException.class);
    private static final Type RUNTIME_EXCEPTION = Type.getType(RuntimeException.class);
    private static final Type OBJECT = Type.getType(Object.class);

    private final MethodDataFlowVerifier methodDataFlowVerifier = new MethodDataFlowVerifier(EXCEPTION, false, RUNTIME_EXCEPTION, asList(INTERFACE, EXCEPTION));

    @Test
    void assignableFrom() {
        assertThat(methodDataFlowVerifier.isAssignableFrom(RUNTIME_EXCEPTION, EXCEPTION)).isTrue();
        assertThat(methodDataFlowVerifier.isAssignableFrom(INTERFACE, EXCEPTION)).isTrue();
        assertThat(methodDataFlowVerifier.isAssignableFrom(OBJECT, EXCEPTION)).isTrue();
    }

    @Test
    void superClass() {
        assertThat(methodDataFlowVerifier.getSuperClass(EXCEPTION)).isEqualTo(RUNTIME_EXCEPTION);
        assertThat(methodDataFlowVerifier.getSuperClass(RUNTIME_EXCEPTION)).isNull();
    }

    @Test
    void isInterface() {
        assertThat(methodDataFlowVerifier.isInterface(EXCEPTION)).isFalse();
        assertThat(methodDataFlowVerifier.isInterface(RUNTIME_EXCEPTION)).isFalse();
        assertThat(methodDataFlowVerifier.isInterface(INTERFACE)).isTrue();
    }

    private interface TestInterface {
    }

    private static class TestException extends RuntimeException implements TestInterface {
    }

}