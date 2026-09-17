package com.buschmais.jqassistant.plugin.java.test.language;

import java.util.Set;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.plugin.common.api.model.AbstractLanguageElementTest;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.*;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement.*;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JavaLanguageElementTest extends AbstractLanguageElementTest {

    public static final String JAVA_SOURCE_FILENAME = "/com/buschmais/Test.java";
    public static final String JAVA_SOURCE_PATH = SOURCE_PATH_PREFIX + JAVA_SOURCE_FILENAME;
    public static final String JAVA_TYPE = "com.buschmais.Type";

    @Test
    public void packageElement() {
        PackageDescriptor descriptor = mock(PackageDescriptor.class);
        when(descriptor.getPath()).thenReturn("src/com/buschmais");
        when(descriptor.getFileName()).thenReturn("/com/buschmais");
        when(descriptor.getFullQualifiedName()).thenReturn("com.buschmais");
        doReturn(Set.of(getArtifactFileDescriptor())).when(descriptor)
            .getParents();

        SourceProvider<PackageDescriptor> sourceProvider = Package.getSourceProvider();
        assertThat(sourceProvider.getName(descriptor)).isEqualTo("com.buschmais");

        verify(descriptor, Package, "com.buschmais", "/com/buschmais");
    }

    @Test
    void typeElement() {
        TypeDescriptor descriptor = getTypeDescriptor();

        verify(descriptor, Type, JAVA_TYPE, JAVA_SOURCE_FILENAME);
    }

    @Test
    void fieldElement() {
        FieldDescriptor descriptor = mock(FieldDescriptor.class);
        doReturn("int value").when(descriptor)
            .getSignature();
        doReturn(getTypeDescriptor()).when(descriptor)
            .getDeclaringType();

        verify(descriptor, Field, "int value", JAVA_SOURCE_FILENAME);
    }

    @Test
    void readFieldElement() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        ReadsDescriptor descriptor = mock(ReadsDescriptor.class);
        doReturn(method).when(descriptor)
            .getMethod();
        doReturn(getTypeDescriptor()).when(method)
            .getDeclaringType();
        doReturn("void doSomething()").when(method)
            .getSignature();
        doReturn(42).when(descriptor)
            .getLineNumber();

        verify(descriptor, ReadField, "void doSomething():42", JAVA_SOURCE_FILENAME, of(42), of(42));
    }

    @Test
    void writeFieldElement() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        WritesDescriptor descriptor = mock(WritesDescriptor.class);
        doReturn(method).when(descriptor)
            .getMethod();
        doReturn(getTypeDescriptor()).when(method)
            .getDeclaringType();
        doReturn("void doSomething()").when(method)
            .getSignature();
        doReturn(42).when(descriptor)
            .getLineNumber();

        verify(descriptor, WriteField, "void doSomething():42", JAVA_SOURCE_FILENAME, of(42), of(42));
    }

    @Test
    void methodInvocationElement() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        InvokesDescriptor descriptor = mock(InvokesDescriptor.class);
        doReturn(method).when(descriptor)
            .getInvokingMethod();
        doReturn(getTypeDescriptor()).when(method)
            .getDeclaringType();
        doReturn("void doSomething()").when(method)
            .getSignature();
        when(descriptor.getLineNumber()).thenReturn(42);

        verify(descriptor, MethodInvocation, "void doSomething():42", JAVA_SOURCE_FILENAME, of(42), of(42));
    }

    @Test
    void methodElement() {
        MethodDescriptor descriptor = mock(MethodDescriptor.class);
        doReturn(getTypeDescriptor()).when(descriptor)
            .getDeclaringType();
        doReturn("int getValue()").when(descriptor)
            .getSignature();
        doReturn(24).when(descriptor)
            .getFirstLineNumber();
        doReturn(42).when(descriptor)
            .getLastLineNumber();

        verify(descriptor, Method, "int getValue()", JAVA_SOURCE_FILENAME, of(24), of(42));
    }

    @Test
    void variableElement() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        VariableDescriptor variable = mock(VariableDescriptor.class);
        doReturn(method).when(variable)
            .getMethod();
        doReturn(getTypeDescriptor()).when(method)
            .getDeclaringType();
        doReturn("void doSomething()").when(method)
            .getSignature();
        doReturn("i").when(variable)
            .getName();
        doReturn("int i").when(variable)
            .getSignature();

        verify(variable, Variable, "void doSomething()#int i", JAVA_SOURCE_FILENAME);
    }

    @Test
    void classFileDependsOnElement() {
        TypeClassFileDescriptor dependent = getTypeDescriptor();
        when(dependent.getFullQualifiedName()).thenReturn("A");
        TypeClassFileDescriptor dependency = mock(TypeClassFileDescriptor.class);
        when(dependency.getFullQualifiedName()).thenReturn("B");
        ClassFileDependsOnDescriptor dependsOnDescriptor = mock(ClassFileDependsOnDescriptor.class);
        when(dependsOnDescriptor.getDependent()).thenReturn(dependent);
        when(dependsOnDescriptor.getDependency()).thenReturn(dependency);

        verify(dependsOnDescriptor, ClassFileDepdendency, "A->B", JAVA_SOURCE_FILENAME);
    }

    private TypeClassFileDescriptor getTypeDescriptor() {
        FileDescriptor packageSourceDirectoryDescriptor = mock(DirectoryDescriptor.class);
        doReturn("src/com/buschmais").when(packageSourceDirectoryDescriptor)
            .getPath();
        PackageDescriptor packageDescriptor = mock(PackageDescriptor.class);
        doReturn(packageSourceDirectoryDescriptor).when(packageDescriptor)
            .getHasSourceFile();

        FileDescriptor typeSourceFileDescriptor = mock(FileDescriptor.class);
        doReturn(JAVA_SOURCE_PATH).when(typeSourceFileDescriptor)
            .getPath();
        doReturn(JAVA_SOURCE_FILENAME).when(typeSourceFileDescriptor)
            .getFileName();
        TypeClassFileDescriptor descriptor = mock(TypeClassFileDescriptor.class);
        doReturn(JAVA_TYPE).when(descriptor)
            .getFullQualifiedName();
        doReturn(typeSourceFileDescriptor).when(descriptor)
            .getHasSourceFile();
        doReturn(Set.of(packageDescriptor, getArtifactFileDescriptor())).when(descriptor)
            .getParents();
        return descriptor;
    }

}
