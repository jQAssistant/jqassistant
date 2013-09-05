package com.buschmais.jqassistant.plugin.java.test.set.dependency.fieldaccesses;

/**
 * A class containing a method with external dependencies to fields.
 */
public class FieldAccess {

    public void readField() {
        FieldDependency fieldDependency = new FieldDependency();
        System.out.println(fieldDependency.field);
    }

    public void writeField() {
        FieldDependency fieldDependency = new FieldDependency();
        fieldDependency.field = null;
    }

    public void readStaticField() {
        System.out.println(FieldDependency.staticField);
    }

    public void writeStaticField() {
        FieldDependency.staticField = null;
    }
}
