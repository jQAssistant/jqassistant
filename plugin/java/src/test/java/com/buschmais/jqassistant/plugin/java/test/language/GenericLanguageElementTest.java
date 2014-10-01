package com.buschmais.jqassistant.plugin.java.test.language;

import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.Field;
import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.Method;
import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.MethodInvocation;
import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.Package;
import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.ReadField;
import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.Type;
import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.WriteField;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.buschmais.jqassistant.core.report.api.SourceProvider;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.InvokesDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ReadsDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.WritesDescriptor;

public class GenericLanguageElementTest {

    // ReadField WriteField Constructor

    @Test
    public void packageName() {
        PackageDirectoryDescriptor descriptor = mock(PackageDirectoryDescriptor.class);
        when(descriptor.getFullQualifiedName()).thenReturn("com.buschmais");
        SourceProvider<PackageDirectoryDescriptor> sourceProvider = (SourceProvider<PackageDirectoryDescriptor>) Package.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("com.buschmais"));
    }

    @Test
    public void typeName() {
        ClassFileDescriptor descriptor = mock(ClassFileDescriptor.class);
        when(descriptor.getFullQualifiedName()).thenReturn("com.buschmais.Type");
        SourceProvider<ClassFileDescriptor> sourceProvider = (SourceProvider<ClassFileDescriptor>) Type.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("com.buschmais.Type"));
    }

    @Test
    public void fieldName() {
        TypeDescriptor type = mock(TypeDescriptor.class);
        FieldDescriptor descriptor = mock(FieldDescriptor.class);
        when(type.getFullQualifiedName()).thenReturn("com.buschmais.Type");
        when(descriptor.getDeclaringType()).thenReturn(type);
        when(descriptor.getSignature()).thenReturn("int value");
        SourceProvider<FieldDescriptor> sourceProvider = (SourceProvider<FieldDescriptor>) Field.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("com.buschmais.Type#int value"));
    }

    @Test
    public void readFieldName() {
        TypeDescriptor type = mock(TypeDescriptor.class);
        MethodDescriptor method = mock(MethodDescriptor.class);
        ReadsDescriptor descriptor = mock(ReadsDescriptor.class);
        when(descriptor.getMethod()).thenReturn(method);
        when(method.getDeclaringType()).thenReturn(type);
        when(type.getFullQualifiedName()).thenReturn("com.buschmais.Type");
        when(method.getSignature()).thenReturn("void doSomething()");
        when(descriptor.getLineNumber()).thenReturn(42);
        SourceProvider<ReadsDescriptor> sourceProvider = (SourceProvider<ReadsDescriptor>) ReadField.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("com.buschmais.Type#void doSomething(), line 42"));
    }

    @Test
    public void writeFieldName() {
        TypeDescriptor type = mock(TypeDescriptor.class);
        MethodDescriptor method = mock(MethodDescriptor.class);
        WritesDescriptor descriptor = mock(WritesDescriptor.class);
        when(descriptor.getMethod()).thenReturn(method);
        when(method.getDeclaringType()).thenReturn(type);
        when(type.getFullQualifiedName()).thenReturn("com.buschmais.Type");
        when(method.getSignature()).thenReturn("void doSomething()");
        when(descriptor.getLineNumber()).thenReturn(42);
        SourceProvider<WritesDescriptor> sourceProvider = (SourceProvider<WritesDescriptor>) WriteField.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("com.buschmais.Type#void doSomething(), line 42"));
    }

    @Test
    public void methodName() {
        TypeDescriptor type = mock(TypeDescriptor.class);
        MethodDescriptor descriptor = mock(MethodDescriptor.class);
        when(type.getFullQualifiedName()).thenReturn("com.buschmais.Type");
        when(descriptor.getDeclaringType()).thenReturn(type);
        when(descriptor.getSignature()).thenReturn("int getValue()");
        SourceProvider<MethodDescriptor> sourceProvider = (SourceProvider<MethodDescriptor>) Method.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("com.buschmais.Type#int getValue()"));
    }

    @Test
    public void methodInvocationName() {
        TypeDescriptor type = mock(TypeDescriptor.class);
        MethodDescriptor method = mock(MethodDescriptor.class);
        InvokesDescriptor descriptor = mock(InvokesDescriptor.class);
        when(descriptor.getInvokingMethod()).thenReturn(method);
        when(method.getDeclaringType()).thenReturn(type);
        when(type.getFullQualifiedName()).thenReturn("com.buschmais.Type");
        when(method.getSignature()).thenReturn("void doSomething()");
        when(descriptor.getLineNumber()).thenReturn(42);
        SourceProvider<InvokesDescriptor> sourceProvider = (SourceProvider<InvokesDescriptor>) MethodInvocation.getSourceProvider();
        String name = sourceProvider.getName(descriptor);
        assertThat(name, equalTo("com.buschmais.Type#void doSomething(), line 42"));
    }
}
