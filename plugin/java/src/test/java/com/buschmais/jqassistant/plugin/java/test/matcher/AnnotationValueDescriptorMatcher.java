package com.buschmais.jqassistant.plugin.java.test.matcher;

import static com.buschmais.jqassistant.plugin.java.test.matcher.TypeDescriptorMatcher.typeDescriptor;

import java.lang.annotation.Annotation;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

/**
 * A matcher for {@link AnnotationValueDescriptor}s.
 */
public class AnnotationValueDescriptorMatcher extends TypeSafeMatcher<AnnotationValueDescriptor> {

	private Matcher<? super TypeDescriptor> typeMatcher;
	private Matcher<?> valueMatcher;

	protected AnnotationValueDescriptorMatcher(Matcher<? super TypeDescriptor> typeMatcher, Matcher<?> valueMatcher) {
		this.typeMatcher = typeMatcher;
		this.valueMatcher = valueMatcher;
	}

	@Override
	protected boolean matchesSafely(AnnotationValueDescriptor item) {
		return typeMatcher.matches(item.getType()) && valueMatcher.matches(item.getValue());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("an annotation value with type '");
		typeMatcher.describeTo(description);
		description.appendText("' and value '");
		valueMatcher.describeTo(description);
		description.appendText("'");
	}

	@Override
	protected void describeMismatchSafely(AnnotationValueDescriptor item, Description mismatchDescription) {
		mismatchDescription.appendText("an annotation value with type '");
		typeMatcher.describeMismatch(item.getType(), mismatchDescription);
		mismatchDescription.appendText("' and value '");
		valueMatcher.describeMismatch(item, mismatchDescription);
		mismatchDescription.appendText("'");
	}

	/**
	 * Return a {@link AnnotationValueDescriptorMatcher} for annotations.
	 * 
	 * @param annotation
	 *            The expected annotation
	 * @param valueMatcher
	 *            The matcher for the annotation values.
	 * @return The {@link AnnotationValueDescriptorMatcher}.
	 */
	public static Matcher<? super AnnotationValueDescriptor> annotationValueDescriptor(Class<? extends Annotation> annotation,
			Matcher<?> valueMatcher) throws NoSuchFieldException {
		return new AnnotationValueDescriptorMatcher(typeDescriptor(annotation), valueMatcher);
	}
}
