package com.buschmais.jqassistant.plugin.common.test.matcher;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;

/**
 * A matcher for {@link FileDescriptor}s.
 *
 * @deprecated Replaced by {@link com.buschmais.jqassistant.plugin.common.test.assertj.FileDescriptorCondition}.
 */
public class FileDescriptorMatcher extends TypeSafeMatcher<FileDescriptor> {

    private Matcher<String> fileNameMatcher;

    private FileDescriptorMatcher(Matcher<String> fileNameMatcher) {
        this.fileNameMatcher = fileNameMatcher;
    }

    @Override
    protected boolean matchesSafely(FileDescriptor item) {
        return fileNameMatcher.matches(item.getFileName());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("file descriptor with name '")
            .appendDescriptionOf(fileNameMatcher)
            .appendText("'");
    }

    public static Matcher<? super FileDescriptor> fileDescriptorMatcher(String fileName) {
        return new FileDescriptorMatcher(Matchers.equalTo(fileName));
    }
}
