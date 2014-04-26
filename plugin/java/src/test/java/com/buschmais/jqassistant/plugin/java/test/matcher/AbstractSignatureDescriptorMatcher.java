package com.buschmais.jqassistant.plugin.java.test.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.SignatureDescriptor;

import java.lang.reflect.Member;

/**
 * A matcher for {@link SignatureDescriptor}s.
 */
public class AbstractSignatureDescriptorMatcher<T extends SignatureDescriptor> extends TypeSafeMatcher<T> {

    private Class<T> type;
    private Member member;
    private String signature;

    AbstractSignatureDescriptorMatcher(Class<T> type, Member member, String signature) {
        this.type = type;
        this.member = member;
        this.signature = signature;
    }

    @Override
    protected boolean matchesSafely(T item) {
        return this.signature.equals(item.getSignature());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(type.getSimpleName()).appendText("(").appendText(signature).appendText(")");
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendText(item.getClass().getSimpleName()).appendText("(").appendText(item.getSignature()).appendText(")");
    }
}
