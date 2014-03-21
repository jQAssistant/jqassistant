package com.buschmais.jqassistant.plugin.java.test.scanner;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.java.test.set.scanner.vsibility.Public;
import org.junit.Test;

import java.io.IOException;

import static com.buschmais.jqassistant.plugin.java.test.matcher.FieldDescriptorMatcher.fieldDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.MethodDescriptorMatcher.methodDescriptor;
import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

public class VisibilityIT extends AbstractPluginIT {

    @Test
    public void publicModifier() throws IOException, NoSuchFieldException, NoSuchMethodException {
        scanClasses(Public.class);
        store.beginTransaction();
        assertThat(query("MATCH (t:TYPE) WHERE t.VISIBILITY='PUBLIC' RETURN t").getColumn("t"), hasItem(typeDescriptor(Public.class)));
        assertThat(query("MATCH (f:FIELD) WHERE f.VISIBILITY='PUBLIC' RETURN f").getColumn("f"), hasItem(fieldDescriptor(Public.class, "field")));
        assertThat(query("MATCH (m:METHOD) WHERE m.VISIBILITY='PUBLIC' RETURN m").getColumn("m"), hasItem(methodDescriptor(Public.class, "method")));
        store.commitTransaction();
    }

    @Test
    public void protectedModifier() throws IOException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException {
        Class<?> innerClass = getInnerClass(Public.class, "Protected");
        scanClasses(innerClass);
        store.beginTransaction();
        assertThat(query("MATCH (t:TYPE) WHERE t.VISIBILITY='PUBLIC' RETURN t").getColumn("t"), hasItem(typeDescriptor(innerClass))); // ?
        assertThat(query("MATCH (f:FIELD) WHERE f.VISIBILITY='PROTECTED' RETURN f").getColumn("f"), hasItem(fieldDescriptor(innerClass, "field")));
        assertThat(query("MATCH (m:METHOD) WHERE m.VISIBILITY='PROTECTED' RETURN m").getColumn("m"), hasItem(methodDescriptor(innerClass, "method")));
        store.commitTransaction();
    }

    @Test
    public void defaultModifier() throws IOException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException {
        Class<?> innerClass = getInnerClass(Public.class, "Default");
        scanClasses(innerClass);
        store.beginTransaction();
        assertThat(query("MATCH (t:TYPE) WHERE t.VISIBILITY='DEFAULT' RETURN t").getColumn("t"), hasItem(typeDescriptor(innerClass)));
        assertThat(query("MATCH (f:FIELD) WHERE f.VISIBILITY='DEFAULT' RETURN f").getColumn("f"), hasItem(fieldDescriptor(innerClass, "field")));
        assertThat(query("MATCH (m:METHOD) WHERE m.VISIBILITY='DEFAULT' RETURN m").getColumn("m"), hasItem(methodDescriptor(innerClass, "method")));
        store.commitTransaction();
    }

    @Test
    public void privateModifier() throws IOException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException {
        Class<?> innerClass = getInnerClass(Public.class, "Private");
        scanClasses(innerClass);
        store.beginTransaction();
        assertThat(query("MATCH (t:TYPE) WHERE t.VISIBILITY='DEFAULT' RETURN t").getColumn("t"), hasItem(typeDescriptor(innerClass))); // ?
        assertThat(query("MATCH (f:FIELD) WHERE f.VISIBILITY='PRIVATE' RETURN f").getColumn("f"), hasItem(fieldDescriptor(innerClass, "field")));
        assertThat(query("MATCH (m:METHOD) WHERE m.VISIBILITY='PRIVATE' RETURN m").getColumn("m"), hasItem(methodDescriptor(innerClass, "method")));
        store.commitTransaction();
    }
}
