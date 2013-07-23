package com.buschmais.jqassistant.scanner.test.matcher;

import com.buschmais.jqassistant.store.api.model.descriptor.AbstractDescriptor;
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

    private Class<T> type;

    private String fullQualifiedName;

    public AbstractDescriptorMatcher(Class<T> type, String fullQualifiedName) {
        this.type = type;
        this.fullQualifiedName = fullQualifiedName;
    }

    @Override
    public boolean matchesSafely(T item) {
        return this.fullQualifiedName.equals(item.getFullQualifiedName());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(type.getName()).appendText("(").appendText(fullQualifiedName).appendText(")");
    }
}