package com.buschmais.jqassistant.plugin.java.test.scanner;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import com.buschmais.jqassistant.plugin.java.test.AbstractJavaPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.vsibility.Public;

public class VisibilityIT extends AbstractJavaPluginIT {

    @Test
    public void publicModifier() throws IOException, NoSuchFieldException, NoSuchMethodException {
        scanClasses(Public.class);
        store.beginTransaction();
        assertThat(query("MATCH (t:Type) WHERE t.visibility='public' RETURN t").getColumn("t"), hasItem(typeDescriptor(Public.class)));
        assertThat(query("MATCH (f:Field) WHERE f.visibility='public' RETURN f").getColumn("f"), hasItem(fieldDescriptor(Public.class, "field")));
        assertThat(query("MATCH (m:Method) WHERE m.visibility='public' RETURN m").getColumn("m"), hasItem(methodDescriptor(Public.class, "method")));
        store.commitTransaction();
    }

    @Test
    public void protectedModifier() throws IOException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException {
        Class<?> innerClass = getInnerClass(Public.class, "Protected");
        scanClasses(innerClass);
        store.beginTransaction();
        assertThat(query("MATCH (t:Type) WHERE t.visibility='public' RETURN t").getColumn("t"), hasItem(typeDescriptor(innerClass))); // ?
        assertThat(query("MATCH (f:Field) WHERE f.visibility='protected' RETURN f").getColumn("f"), hasItem(fieldDescriptor(innerClass, "field")));
        assertThat(query("MATCH (m:Method) WHERE m.visibility='protected' RETURN m").getColumn("m"), hasItem(methodDescriptor(innerClass, "method")));
        store.commitTransaction();
    }

    @Test
    public void defaultModifier() throws IOException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException {
        Class<?> innerClass = getInnerClass(Public.class, "Default");
        scanClasses(innerClass);
        store.beginTransaction();
        assertThat(query("MATCH (t:Type) WHERE t.visibility='default' RETURN t").getColumn("t"), hasItem(typeDescriptor(innerClass)));
        assertThat(query("MATCH (f:Field) WHERE f.visibility='default' RETURN f").getColumn("f"), hasItem(fieldDescriptor(innerClass, "field")));
        assertThat(query("MATCH (m:Method) WHERE m.visibility='default' RETURN m").getColumn("m"), hasItem(methodDescriptor(innerClass, "method")));
        store.commitTransaction();
    }

    @Test
    public void privateModifier() throws IOException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException {
        Class<?> innerClass = getInnerClass(Public.class, "Private");
        scanClasses(innerClass);
        store.beginTransaction();
        assertThat(query("MATCH (t:Type) WHERE t.visibility='default' RETURN t").getColumn("t"), hasItem(typeDescriptor(innerClass))); // ?
        assertThat(query("MATCH (f:Field) WHERE f.visibility='private' RETURN f").getColumn("f"), hasItem(fieldDescriptor(innerClass, "field")));
        assertThat(query("MATCH (m:Method) WHERE m.visibility='private' RETURN m").getColumn("m"), hasItem(methodDescriptor(innerClass, "method")));
        store.commitTransaction();
    }
}
