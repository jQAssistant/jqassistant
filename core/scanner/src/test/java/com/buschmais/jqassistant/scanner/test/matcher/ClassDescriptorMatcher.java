package com.buschmais.jqassistant.scanner.test.matcher;

import com.buschmais.jqassistant.store.api.model.ClassDescriptor;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 14.07.13
 * Time: 17:10
 * To change this template use File | Settings | File Templates.
 */
public class ClassDescriptorMatcher extends AbstractDescriptorMatcher<ClassDescriptor> {

    public ClassDescriptorMatcher(Class<?> type) {
        super(ClassDescriptor.class, type.getName());
    }

    public static ClassDescriptorMatcher classDescriptor(Class<?> type) {
        return new ClassDescriptorMatcher(type);
    }

}
