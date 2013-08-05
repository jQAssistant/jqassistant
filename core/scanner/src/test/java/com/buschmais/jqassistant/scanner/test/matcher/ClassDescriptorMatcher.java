package com.buschmais.jqassistant.scanner.test.matcher;

import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import org.hamcrest.Matcher;

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

    public static Matcher<ClassDescriptor> classDescriptor(Class<?> type) {
        return new ClassDescriptorMatcher(type);
    }

}
