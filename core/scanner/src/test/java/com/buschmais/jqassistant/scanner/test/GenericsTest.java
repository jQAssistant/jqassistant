package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.scanner.test.set.generics.*;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class GenericsTest extends AbstractScannerTest {

    @Test
    public void genericType() throws IOException {
        TypeDescriptor genericType = stubClass(GenericType.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.createMethodDescriptor(genericType, "void <init>()")).thenReturn(constructor);

        scanner.scanClasses(GenericType.class);

        assertThat(genericType.getSuperClass(), equalTo(javaLangObject));
        assertThat(genericType.getContains(), hasItem(constructor));
        assertThat(constructor.getDependencies(), hasItem(_void));
    }

    @Test
    public void boundGenericType() throws IOException {
        TypeDescriptor boundGenericType = stubClass(BoundGenericType.class);
        TypeDescriptor javaLangNumber = stubClass(Number.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.createMethodDescriptor(boundGenericType, "void <init>()")).thenReturn(constructor);

        scanner.scanClasses(BoundGenericType.class);

        assertThat(boundGenericType.getSuperClass(), equalTo(javaLangObject));
        assertThat(boundGenericType.getContains(), hasItem(constructor));
        assertThat(boundGenericType.getDependencies(), hasItem(javaLangNumber));
        assertThat(constructor.getDependencies(), hasItem(_void));
    }

    @Test
    public void nestedGenericType() throws IOException {
        TypeDescriptor nestedGenericType = stubClass(NestedGenericType.class);
        TypeDescriptor genericType = stubClass(GenericType.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.createMethodDescriptor(nestedGenericType, "void <init>()")).thenReturn(constructor);

        scanner.scanClasses(NestedGenericType.class);

        assertThat(nestedGenericType.getSuperClass(), equalTo(javaLangObject));
        assertThat(nestedGenericType.getContains(), hasItem(constructor));
        assertThat(nestedGenericType.getDependencies(), hasItem(genericType));
        assertThat(constructor.getDependencies(), hasItem(_void));
    }

    @Test
    public void nestedGenericMethod() throws IOException {
        TypeDescriptor nestedGenericType = stubClass(NestedGenericMethod.class);
        TypeDescriptor genericType = stubClass(GenericType.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.createMethodDescriptor(nestedGenericType, "void <init>()")).thenReturn(constructor);
        MethodDescriptor get = new MethodDescriptor();
        when(store.createMethodDescriptor(nestedGenericType, "java.lang.Object get(" + GenericType.class.getName() + ")")).thenReturn(get);

        scanner.scanClasses(NestedGenericMethod.class);

        assertThat(nestedGenericType.getSuperClass(), equalTo(javaLangObject));
        assertThat(nestedGenericType.getContains(), hasItem(constructor));
        assertThat(constructor.getDependencies(), hasItem(_void));
        assertThat(get.getDependencies(), hasItem(genericType));
    }

    @Test
    public void extendsGenericClass() throws IOException {
        TypeDescriptor extendsGenericClass = stubClass(ExtendsGenericClass.class);
        TypeDescriptor genericType = stubClass(GenericType.class);
        TypeDescriptor javaLangNumber = stubClass(Number.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.createMethodDescriptor(extendsGenericClass, "void <init>()")).thenReturn(constructor);

        scanner.scanClasses(ExtendsGenericClass.class);

        assertThat(extendsGenericClass.getSuperClass(), equalTo(genericType));
        assertThat(extendsGenericClass.getDependencies(), hasItem(javaLangNumber));
        assertThat(extendsGenericClass.getContains(), hasItem(constructor));
        assertThat(constructor.getDependencies(), hasItem(_void));
    }

    @Test
    public void implementsGenericInterface() throws IOException {
        TypeDescriptor extendsGenericClass = stubClass(ImplementsGenericInterface.class);
        TypeDescriptor javaUtilIterable = stubClass(Iterable.class);
        TypeDescriptor javaLangNumber = stubClass(Number.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.createMethodDescriptor(extendsGenericClass, "void <init>()")).thenReturn(constructor);

        scanner.scanClasses(ImplementsGenericInterface.class);

        assertThat(extendsGenericClass.getInterfaces(), hasItem(javaUtilIterable));
        assertThat(extendsGenericClass.getDependencies(), hasItem(javaLangNumber));
        assertThat(extendsGenericClass.getContains(), hasItem(constructor));
        assertThat(constructor.getDependencies(), hasItem(_void));
    }
}
