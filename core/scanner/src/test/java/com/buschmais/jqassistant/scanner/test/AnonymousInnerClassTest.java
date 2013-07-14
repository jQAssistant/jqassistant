package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.test.sets.innerclass.AnonymousInnerClass;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.FieldDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class AnonymousInnerClassTest extends AbstractScannerTest {

    @Test
    public void outerClass() throws IOException {
        ClassDescriptor outerClassType = stubClass(AnonymousInnerClass.class);
        ClassDescriptor innerClassType = stubClass(AnonymousInnerClass.class.getPackage().getName(), AnonymousInnerClass.class.getSimpleName() + "$1");
        ClassDescriptor iteratorType = stubClass(Iterator.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.resolveMethodDescriptor(outerClassType, "void <init>()")).thenReturn(constructor);
        MethodDescriptor iterator = new MethodDescriptor();
        when(store.resolveMethodDescriptor(outerClassType, "java.util.Iterator iterator()")).thenReturn(iterator);

        scanner.scanClasses(AnonymousInnerClass.class);

        assertThat(outerClassType.getSuperClass(), equalTo(javaLangObject));
        assertThat(outerClassType.getContains(), hasItem(constructor));
        assertThat(outerClassType.getContains(), hasItem(iterator));
        assertThat(iterator.getDependencies(), hasItem(innerClassType));
    }

    @Test
    @Ignore
    public void innerClass() throws IOException {
        ClassDescriptor outerClassType = stubClass(AnonymousInnerClass.class);
        ClassDescriptor innerClassType = stubClass(AnonymousInnerClass.class.getPackage().getName(), AnonymousInnerClass.class.getSimpleName() + "$1");
        ClassDescriptor iteratorType = stubClass(Iterator.class);
        MethodDescriptor constructor = new MethodDescriptor();
        when(store.resolveMethodDescriptor(outerClassType, "void <init>()")).thenReturn(constructor);
        MethodDescriptor hasNext = new MethodDescriptor();
        when(store.resolveMethodDescriptor(outerClassType, "boolean hasNext()")).thenReturn(hasNext);
        MethodDescriptor next = new MethodDescriptor();
        when(store.resolveMethodDescriptor(outerClassType, "Object next()")).thenReturn(next);
        MethodDescriptor remove = new MethodDescriptor();
        when(store.resolveMethodDescriptor(outerClassType, "void remove()")).thenReturn(remove);
        FieldDescriptor this_0 = new FieldDescriptor();
        when(store.resolveFieldDescriptor(innerClassType, AnonymousInnerClass.class.getName() + " this$0")).thenReturn(this_0);

        String resourceName = "/" + AnonymousInnerClass.class.getName().replace(".", "/") + "$1.class";
        InputStream is = AnonymousInnerClassTest.class.getResourceAsStream(resourceName);
        scanner.scanInputStream(is, "");

        assertThat(innerClassType.getSuperClass(), equalTo(javaLangObject));
        assertThat(innerClassType.getContains(), hasItem(constructor));
        assertThat(innerClassType.getContains(), hasItem(hasNext));
        assertThat(innerClassType.getContains(), hasItem(next));
        assertThat(innerClassType.getContains(), hasItem(remove));
    }
}
