package com.buschmais.jqassistant.plugin.java.test;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionIT extends AbstractJavaPluginIT {

    @Test
    void throwNewIllegalArgumentException() {
        verify("throwNewIllegalArgumentException", IllegalArgumentException.class);
    }

    @Test
    void throwRuntimeExceptionFromParameter() {
        verify("throwRuntimeExceptionFromParameter", RuntimeException.class);
    }

    private void verify(String methodName, Class<?> expectedExceptionType) {
        scanClasses(TestClass.class);
        store.beginTransaction();
        TestResult query = query("MATCH (:Method{name:$methodName})-[:THROWS]->(exception:Type) RETURN exception", Map.of("methodName", methodName));
        List<TypeDescriptor> exception = query.getColumn("exception");
        assertThat(exception).hasSize(1)
                .haveExactly(1, typeDescriptor(expectedExceptionType));
        store.commitTransaction();
    }

    private static final class TestClass {
        void throwNewIllegalArgumentException() {
            throw new IllegalArgumentException("Illegal argument exception.");
        }

        void throwRuntimeExceptionFromParameter(RuntimeException e) {
            throw e;
        }
    }

}
