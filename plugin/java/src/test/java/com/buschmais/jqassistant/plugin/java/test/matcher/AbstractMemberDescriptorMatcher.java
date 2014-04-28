package com.buschmais.jqassistant.plugin.java.test.matcher;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MemberDescriptor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.Member;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;

/**
 * A matcher for
 * {@link com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MemberDescriptor}
 * s.
 */
public class AbstractMemberDescriptorMatcher<T extends MemberDescriptor> extends TypeSafeMatcher<T> {

    private Class<T> type;
    private Member member;
    private String signature;

    AbstractMemberDescriptorMatcher(Class<T> type, Member member, String signature) {
        this.type = type;
        this.member = member;
        this.signature = signature;
    }

    @Override
    protected boolean matchesSafely(T item) {
        return typeDescriptor(member.getDeclaringClass()).matches(item.getDeclaringType()) && this.signature.equals(item.getSignature());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(type.getSimpleName()).appendText("(").appendText(member.getDeclaringClass().getName()).appendText("#").appendText(signature)
                .appendText(")");
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendText(item.getClass().getSimpleName()).appendText("(").appendText(item.getDeclaringType().getFullQualifiedName())
                .appendText("#").appendText(item.getSignature()).appendText(")");
    }
}
