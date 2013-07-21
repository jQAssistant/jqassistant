package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.test.sets.generics.*;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class GenericsTest extends AbstractScannerTest {

    @Test
    public void genericType() throws IOException {
        ClassDescriptor genericType = stubClass(GenericType.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.createMethodDescriptor(genericType, "void <init>()")).thenReturn(constructor);

        scanner.scanClasses(GenericType.class);

        assertThat(genericType.getSuperClass(), equalTo(javaLangObject));
        assertThat(genericType.getContains(), hasItem(constructor));
        assertThat(constructor.getDependencies(), hasItem(_void));
    }

    @Test
    public void boundGenericType() throws IOException {
        ClassDescriptor boundGenericType = stubClass(BoundGenericType.class);
        ClassDescriptor javaLangNumber = stubClass(Number.class);
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
        ClassDescriptor nestedGenericType = stubClass(NestedGenericType.class);
        ClassDescriptor genericType = stubClass(GenericType.class);
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
        ClassDescriptor nestedGenericType = stubClass(NestedGenericMethod.class);
        ClassDescriptor genericType = stubClass(GenericType.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.createMethodDescriptor(nestedGenericType, "void <init>()")).thenReturn(constructor);
        MethodDescriptor get = new MethodDescriptor();
        when(store.createMethodDescriptor(nestedGenericType, "java.lang.Object get(com.buschmais.jqassistant.scanner.test.sets.generics.GenericType)")).thenReturn(get);

        scanner.scanClasses(NestedGenericMethod.class);

        assertThat(nestedGenericType.getSuperClass(), equalTo(javaLangObject));
        assertThat(nestedGenericType.getContains(), hasItem(constructor));
        assertThat(constructor.getDependencies(), hasItem(_void));
        assertThat(get.getDependencies(), hasItem(genericType));
    }

    @Test
    public void extendsGenericClass() throws IOException {
        ClassDescriptor extendsGenericClass = stubClass(ExtendsGenericClass.class);
        ClassDescriptor genericType = stubClass(GenericType.class);
        ClassDescriptor javaLangNumber = stubClass(Number.class);
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
        ClassDescriptor extendsGenericClass = stubClass(ImplementsGenericInterface.class);
        ClassDescriptor javaUtilIterable = stubClass(Iterable.class);
        ClassDescriptor javaLangNumber = stubClass(Number.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.createMethodDescriptor(extendsGenericClass, "void <init>()")).thenReturn(constructor);

        scanner.scanClasses(ImplementsGenericInterface.class);

        assertThat(extendsGenericClass.getInterfaces(), hasItem(javaUtilIterable));
        assertThat(extendsGenericClass.getDependencies(), hasItem(javaLangNumber));
        assertThat(extendsGenericClass.getContains(), hasItem(constructor));
        assertThat(constructor.getDependencies(), hasItem(_void));
    }
}
