package com.buschmais.jqassistant.scanner.test.matcher;

import com.buschmais.jqassistant.store.api.model.descriptor.FieldDescriptor;

import java.lang.reflect.Field;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 14.07.13
 * Time: 17:10
 * To change this template use File | Settings | File Templates.
 */
public class FieldDescriptorMatcher extends AbstractDescriptorMatcher<FieldDescriptor> {

    public FieldDescriptorMatcher(Field field) {
        super(FieldDescriptor.class, field.getDeclaringClass().getName() + "#" + field.getType().getCanonicalName() + " " + field.getName());
    }

    public static FieldDescriptorMatcher fieldDescriptor(Field field) {
        return new FieldDescriptorMatcher(field);
    }

}
