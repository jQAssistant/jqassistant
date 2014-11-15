package com.buschmais.jqassistant.plugin.java.test.matcher;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;

import java.lang.reflect.Member;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import com.buschmais.jqassistant.plugin.java.api.model.MemberDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * A matcher for
 * {@link com.buschmais.jqassistant.plugin.java.api.model.MemberDescriptor} s.
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
        mismatchDescription.appendText(item.getClass().getSimpleName());
        TypeDescriptor declaringType = item.getDeclaringType();
        if (declaringType != null) {
            mismatchDescription.appendText("(").appendText(declaringType.getFullQualifiedName());
        }
        mismatchDescription.appendText("#").appendText(item.getSignature()).appendText(")");
    }
}
