package com.buschmais.jqassistant.plugin.java.test.matcher;

import com.buschmais.jqassistant.plugin.common.api.model.PropertyDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PropertyFileDescriptor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PropertyFileDescriptorMatchers {

    public static Matcher<PropertyFileDescriptor> hasProperties(int size) {
        return PropertyFileDescriptorSizeMatcher.hasSize(size);
    }

    public static Matcher<PropertyFileDescriptor> hasNoProperties() {
        return new EmptyPropertyFileDescriptorMatcher();
    }

    public static Matcher<PropertyFileDescriptor> containsProperties(Matcher<? super PropertyDescriptor>... matchers) {
        return new TypeSafeMatcher<PropertyFileDescriptor>() {
            @Override
            protected boolean matchesSafely(PropertyFileDescriptor descriptor) {
                List<PropertyDescriptor> properties = descriptor.getProperties();

                for (Matcher<? super PropertyDescriptor> matcher : matchers) {
                    Optional<PropertyDescriptor> match = properties.stream().filter(matcher::matches).findFirst();

                    if (!match.isPresent()) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A " + PropertyFileDescriptor.class.getSimpleName())
                           .appendText(" with at least ");

                for (int i = 0; i < matchers.length; i++) {
                    matchers[i].describeTo(description);

                    if (i < matchers.length - 1) {
                        description.appendText(", ");
                    }
                }
            }

            @Override
            protected void describeMismatchSafely(PropertyFileDescriptor item, Description mismatchDescription) {
                mismatchDescription.appendText("A " + PropertyFileDescriptor.class.getSimpleName())
                                   .appendText(" with ");

                String line = item.getProperties().stream().map(pd -> String.format("a property with name '%s' and " +
                                                                                    "value '%s'", pd.getName(),
                                                                                    pd.getValue())).collect
                    (Collectors.joining(", "));

                mismatchDescription.appendText(line);
            }
        };
    }


    private static class PropertyFileDescriptorSizeMatcher extends TypeSafeMatcher<PropertyFileDescriptor> {
        private final int size;

        PropertyFileDescriptorSizeMatcher(int size) {
            this.size = size;
        }

        static Matcher<PropertyFileDescriptor> hasSize(int size) {
            return new PropertyFileDescriptorSizeMatcher(size);
        }

        @Override
        protected boolean matchesSafely(PropertyFileDescriptor item) {
            int entriesCount = item.getProperties().size();

            return entriesCount == size;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("A ").appendText(PropertyFileDescriptor.class.getSimpleName());
            if (size == 0 || size >= 2) {
                description.appendText(" with ").appendValue(size)
                           .appendText(" entries.");
            } else {
                description.appendText(" with ").appendValue(size)
                           .appendText(" entry");
            }
        }

        @Override
        protected void describeMismatchSafely(PropertyFileDescriptor descriptor, Description mismatchDescription) {
            mismatchDescription.appendText("A ")
                               .appendText(PropertyFileDescriptor.class.getSimpleName());

            int actualSize = descriptor.getProperties().size();

            if (actualSize == 0 || actualSize >= 2) {
                mismatchDescription.appendText(" with ").appendValue(actualSize)
                                   .appendText(" entries.");
            } else {
                mismatchDescription.appendText(" with ").appendValue(actualSize)
                                   .appendText(" entry");
            }

        }
    }

    private static class EmptyPropertyFileDescriptorMatcher extends TypeSafeMatcher<PropertyFileDescriptor> {
        @Override
        protected boolean matchesSafely(PropertyFileDescriptor item) {
            return item.getProperties().isEmpty();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("A ").appendText(PropertyFileDescriptor.class.getSimpleName())
                       .appendText(" no entries.");
        }

        @Override
        protected void describeMismatchSafely(PropertyFileDescriptor descriptor, Description mismatchDescription) {
            mismatchDescription.appendText("A ")
                               .appendText(PropertyFileDescriptor.class.getSimpleName());

            int actualSize = descriptor.getProperties().size();

            if (actualSize == 0 || actualSize >= 2) {
                mismatchDescription.appendText(" with ").appendValue(actualSize)
                                   .appendText(" entries.");
            } else {
                mismatchDescription.appendText(" with ").appendValue(actualSize)
                                   .appendText(" entry");
            }

        }

    }
}
