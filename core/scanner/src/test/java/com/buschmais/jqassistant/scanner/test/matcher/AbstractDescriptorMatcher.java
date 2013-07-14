package com.buschmais.jqassistant.scanner.test.matcher;

import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Created with IntelliJ IDEA.
 * User: Dirk Mahler
 * Date: 14.07.13
 * Time: 17:06
 * To change this template use File | Settings | File Templates.
 */
public class AbstractDescriptorMatcher<T extends AbstractDescriptor> extends TypeSafeMatcher<T> {

    private String fullQualifiedName;

    public AbstractDescriptorMatcher(String fullQualifiedName) {
        this.fullQualifiedName = fullQualifiedName;
    }

    @Override
    public boolean matchesSafely(T item) {
        return this.fullQualifiedName.equals(item.getFullQualifiedName());
    }

    @Override
    public void describeTo(Description description) {

    }
}