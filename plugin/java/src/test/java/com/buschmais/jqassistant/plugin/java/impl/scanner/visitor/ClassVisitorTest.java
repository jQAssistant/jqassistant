package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.RecordComponentVisitor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that {@link ClassVisitor} handles null cachedType gracefully,
 * e.g. when processing module-info.class where cachedType is never initialized.
 */
@ExtendWith(MockitoExtension.class)
class ClassVisitorTest {

    @Mock
    private FileDescriptor fileDescriptor;

    @Mock
    private VisitorHelper visitorHelper;

    private ClassVisitor classVisitor;

    @BeforeEach
    void setUp() {
        // cachedType remains null (simulates module-info.class scenario)
        classVisitor = new ClassVisitor(fileDescriptor, visitorHelper);
    }

    @Test
    void visitRecordComponentWithNullCachedType() {
        RecordComponentVisitor result = classVisitor.visitRecordComponent("name", "Ljava/lang/String;", null);
        assertThat(result).isNull();
    }

    @Test
    void visitFieldWithNullCachedType() {
        FieldVisitor result = classVisitor.visitField(0, "field", "I", null, null);
        assertThat(result).isNull();
    }

    @Test
    void visitMethodWithNullCachedType() {
        MethodVisitor result = classVisitor.visitMethod(0, "method", "()V", null, null);
        assertThat(result).isNull();
    }

    @Test
    void visitInnerClassWithNullCachedType() {
        classVisitor.visitInnerClass("Inner", "Outer", "Inner", 0);
        // should not throw NPE
    }

    @Test
    void visitOuterClassWithNullCachedType() {
        classVisitor.visitOuterClass("Outer", "method", "()V");
        // should not throw NPE
    }

    @Test
    void visitAnnotationWithNullCachedType() {
        AnnotationVisitor result = classVisitor.visitAnnotation("Ljava/lang/Deprecated;", true);
        assertThat(result).isNull();
    }
}
