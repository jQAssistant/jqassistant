package com.buschmais.jqassistant.plugin.java.test.language;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.plugin.common.api.model.AbstractLanguageElementTest;
import com.buschmais.jqassistant.plugin.java.api.model.*;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement.*;
import static com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement.Package;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JavaLanguageElementTest extends AbstractLanguageElementTest {

    @Test
    public void packageElement() {
        PackageDescriptor descriptor = mock(PackageDescriptor.class);
        when(descriptor.getFileName()).thenReturn("/com/buschmais");
        when(descriptor.getFullQualifiedName()).thenReturn("com.buschmais");
        doReturn(newHashSet(getArtifactFileDescriptor())).when(descriptor).getParents();

        SourceProvider<PackageDescriptor> sourceProvider = Package.getSourceProvider();
        assertThat(sourceProvider.getName(descriptor)).isEqualTo("com.buschmais");

        verify(descriptor, Package, "com.buschmais", "/com/buschmais");
    }

    @Test
    void typeElement() {
        TypeDescriptor descriptor = getTypeDescriptor();

        verify(descriptor, Type, "com.buschmais.Type", "/com/buschmais/Test.java");
    }

    @Test
    void fieldElement() {
        FieldDescriptor descriptor = mock(FieldDescriptor.class);
        doReturn("int value").when(descriptor).getSignature();
        doReturn(getTypeDescriptor()).when(descriptor).getDeclaringType();

        verify(descriptor, Field, "int value", "/com/buschmais/Test.java");
    }

    @Test
    void readFieldElement() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        ReadsDescriptor descriptor = mock(ReadsDescriptor.class);
        doReturn(method).when(descriptor).getMethod();
        doReturn(getTypeDescriptor()).when(method).getDeclaringType();
        doReturn("void doSomething()").when(method).getSignature();
        doReturn(42).when(descriptor).getLineNumber();

        verify(descriptor, ReadField, "void doSomething(), line 42", "/com/buschmais/Test.java", of(42), of(42));
    }

    @Test
    void writeFieldElement() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        WritesDescriptor descriptor = mock(WritesDescriptor.class);
        doReturn(method).when(descriptor).getMethod();
        doReturn(getTypeDescriptor()).when(method).getDeclaringType();
        doReturn("void doSomething()").when(method).getSignature();
        doReturn(42).when(descriptor).getLineNumber();

        verify(descriptor, WriteField, "void doSomething(), line 42", "/com/buschmais/Test.java", of(42), of(42));
    }

    @Test
    void methodInvocationElement() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        InvokesDescriptor descriptor = mock(InvokesDescriptor.class);
        doReturn(method).when(descriptor).getInvokingMethod();
        doReturn(getTypeDescriptor()).when(method).getDeclaringType();
        doReturn("void doSomething()").when(method).getSignature();
        when(descriptor.getLineNumber()).thenReturn(42);

        verify(descriptor, MethodInvocation, "void doSomething(), line 42", "/com/buschmais/Test.java", of(42), of(42));
    }

    @Test
    void methodElement() {
        MethodDescriptor descriptor = mock(MethodDescriptor.class);
        doReturn(getTypeDescriptor()).when(descriptor).getDeclaringType();
        doReturn("int getValue()").when(descriptor).getSignature();
        doReturn(24).when(descriptor).getFirstLineNumber();
        doReturn(42).when(descriptor).getLastLineNumber();

        verify(descriptor, Method, "int getValue()", "/com/buschmais/Test.java", of(24), of(42));
    }

    @Test
    void variableElement() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        VariableDescriptor variable = mock(VariableDescriptor.class);
        doReturn(method).when(variable).getMethod();
        doReturn(getTypeDescriptor()).when(method).getDeclaringType();
        doReturn("void doSomething()").when(method).getSignature();
        doReturn("i").when(variable).getName();
        doReturn("int i").when(variable).getSignature();

        verify(variable, Variable, "void doSomething()#int i", "/com/buschmais/Test.java");
    }

    @Test
    void typeDependsOnElement() {
        TypeDescriptor dependent = getTypeDescriptor();
        when(dependent.getName()).thenReturn("A");
        TypeDescriptor dependency = mock(TypeDescriptor.class);
        when(dependency.getName()).thenReturn("B");
        TypeDependsOnDescriptor dependsOnDescriptor = mock(TypeDependsOnDescriptor.class);
        when(dependsOnDescriptor.getDependent()).thenReturn(dependent);
        when(dependsOnDescriptor.getDependency()).thenReturn(dependency);

        verify(dependsOnDescriptor, TypeDepdendency, "A->B", "/com/buschmais/Test.java");
    }

    private TypeDescriptor getTypeDescriptor() {
        PackageDescriptor packageDescriptor = mock(PackageDescriptor.class);
        doReturn("/com/buschmais").when(packageDescriptor).getFileName();
        ClassFileDescriptor descriptor = mock(ClassFileDescriptor.class);
        doReturn("/com/buschmais/Test.class").when(descriptor).getFileName();
        doReturn("Test.java").when(descriptor).getSourceFileName();
        doReturn("com.buschmais.Type").when(descriptor).getFullQualifiedName();
        doReturn(newHashSet(packageDescriptor, getArtifactFileDescriptor())).when(descriptor).getParents();
        return descriptor;
    }

}
