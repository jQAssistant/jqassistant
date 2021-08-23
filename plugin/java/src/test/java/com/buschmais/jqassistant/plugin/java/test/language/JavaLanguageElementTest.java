package com.buschmais.jqassistant.plugin.java.test.language;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.plugin.java.api.model.*;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement.*;
import static com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement.Package;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JavaLanguageElementTest {

    // ReadField WriteField Constructor

    @Test
    public void packageName() {
        PackageDescriptor descriptor = mock(PackageDescriptor.class);
        when(descriptor.getFullQualifiedName()).thenReturn("com.buschmais");
        SourceProvider<PackageDescriptor> sourceProvider = Package.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("com.buschmais"));
    }

    @Test
    void typeName() {
        ClassFileDescriptor descriptor = mock(ClassFileDescriptor.class);
        when(descriptor.getFullQualifiedName()).thenReturn("com.buschmais.Type");
        SourceProvider<ClassFileDescriptor> sourceProvider = Type.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("com.buschmais.Type"));
    }

    @Test
    void fieldName() {
        FieldDescriptor descriptor = mock(FieldDescriptor.class);
        when(descriptor.getSignature()).thenReturn("int value");
        SourceProvider<FieldDescriptor> sourceProvider = Field.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("int value"));
    }

    @Test
    void readFieldName() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        ReadsDescriptor descriptor = mock(ReadsDescriptor.class);
        when(descriptor.getMethod()).thenReturn(method);
        when(method.getSignature()).thenReturn("void doSomething()");
        when(descriptor.getLineNumber()).thenReturn(42);
        SourceProvider<ReadsDescriptor> sourceProvider = ReadField.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("void doSomething(), line 42"));
    }

    @Test
    void writeFieldName() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        WritesDescriptor descriptor = mock(WritesDescriptor.class);
        when(descriptor.getMethod()).thenReturn(method);
        when(method.getSignature()).thenReturn("void doSomething()");
        when(descriptor.getLineNumber()).thenReturn(42);
        SourceProvider<WritesDescriptor> sourceProvider = WriteField.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("void doSomething(), line 42"));
    }

    @Test
    void methodName() {
        MethodDescriptor descriptor = mock(MethodDescriptor.class);
        when(descriptor.getSignature()).thenReturn("int getValue()");
        SourceProvider<MethodDescriptor> sourceProvider = Method.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("int getValue()"));
    }

    @Test
    void variableName() {
        ClassFileDescriptor type = mock(ClassFileDescriptor.class);
        MethodDescriptor method = mock(MethodDescriptor.class);
        VariableDescriptor variable = mock(VariableDescriptor.class);
        when(method.getDeclaringType()).thenReturn(type);
        when(type.getFileName()).thenReturn("/com/buschmais/Type");
        when(method.getSignature()).thenReturn("void doSomething()");
        when(method.getFirstLineNumber()).thenReturn(10);
        when(method.getLastLineNumber()).thenReturn(20);
        when(variable.getMethod()).thenReturn(method);
        when(variable.getName()).thenReturn("i");
        when(variable.getSignature()).thenReturn("int i");
        SourceProvider<VariableDescriptor> sourceProvider = Variable.getSourceProvider();
        assertThat(sourceProvider.getName(variable), equalTo("void doSomething()#int i"));
        assertThat(sourceProvider.getLineNumber(variable), equalTo(10));
        assertThat(sourceProvider.getSourceFile(variable), equalTo("/com/buschmais/Type"));
    }

    @Test
    void methodInvocationName() {
        MethodDescriptor method = mock(MethodDescriptor.class);
        InvokesDescriptor descriptor = mock(InvokesDescriptor.class);
        when(descriptor.getInvokingMethod()).thenReturn(method);
        when(method.getSignature()).thenReturn("void doSomething()");
        when(descriptor.getLineNumber()).thenReturn(42);
        SourceProvider<InvokesDescriptor> sourceProvider = MethodInvocation.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("void doSomething(), line 42"));
    }

    @Test
    void typeDependsOnName() {
        TypeDescriptor dependent = mock(TypeDescriptor.class);
        when(dependent.getName()).thenReturn("A");
        TypeDescriptor dependency = mock(TypeDescriptor.class);
        when(dependency.getName()).thenReturn("B");
        TypeDependsOnDescriptor dependsOnDescriptor = mock(TypeDependsOnDescriptor.class);
        when(dependsOnDescriptor.getDependent()).thenReturn(dependent);
        when(dependsOnDescriptor.getDependency()).thenReturn(dependency);
        SourceProvider<TypeDependsOnDescriptor> sourceProvider = TypeDepdendency.getSourceProvider();
        String name = sourceProvider.getName(dependsOnDescriptor);
        assertThat(name, equalTo("A->B"));
    }
}
