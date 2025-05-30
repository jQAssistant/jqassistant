package com.buschmais.jqassistant.plugin.java.test.scanner;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.buschmais.jqassistant.plugin.java.api.model.ThrowableDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ThrowsDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.test.assertj.TypeDescriptorCondition.typeDescriptor;
import static org.assertj.core.api.Assertions.assertThat;

class ThrowsExceptionIT extends AbstractJavaPluginIT {

    @Test
    void throwNewIllegalArgumentException() {
        verify("throwNewIllegalArgumentException", IllegalArgumentException.class, throwsDescriptor -> {
            assertThat(throwsDescriptor.isDeclaration()).isFalse();
            assertThat(throwsDescriptor.getLineNumber()).isNotNull();
        });
    }

    @Test
    void throwRuntimeExceptionFromParameter() {
        verify("throwRuntimeExceptionFromParameter", RuntimeException.class, throwsDescriptor -> {
            assertThat(throwsDescriptor.isDeclaration()).isFalse();
            assertThat(throwsDescriptor.getLineNumber()).isNotNull();
        });
    }

    @Test
    void declaredException() {
        verify("declaredException", Exception.class, throwsDescriptor -> {
            assertThat(throwsDescriptor.isDeclaration()).isTrue();
            assertThat(throwsDescriptor.getLineNumber()).isNull();
        });
    }

    private void verify(String methodName, Class<?> expectedExceptionType, Consumer<ThrowsDescriptor> throwsDescriptorConsumer) {
        scanClasses(TestClass.class);
        store.beginTransaction();
        TestResult query = query("MATCH (:Method{name:$methodName})-[throwsException:THROWS]->(:Type:Throwable) RETURN throwsException",
            Map.of("methodName", methodName));
        List<ThrowsDescriptor> throwsExceptions = query.getColumn("throwsException");
        assertThat(throwsExceptions).hasSize(1);
        ThrowsDescriptor throwsDescriptor = throwsExceptions.get(0);
        TypeDescriptor thrownType = throwsDescriptor.getThrownType();
        assertThat(thrownType).isInstanceOf(ThrowableDescriptor.class)
            .is(typeDescriptor(expectedExceptionType));
        throwsDescriptorConsumer.accept(throwsDescriptor);
        store.commitTransaction();
    }

    private static final class TestClass {
        void throwNewIllegalArgumentException() {
            throw new IllegalArgumentException("Illegal argument exception.");
        }

        void throwRuntimeExceptionFromParameter(RuntimeException e) {
            throw e;
        }

        void declaredException() throws Exception {
            // only the declaration is required for the test
        }
    }

}
