package com.buschmais.jqassistant.plugin.common.test.matcher;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;

/**
 * A matcher for {@link FileDescriptor}s.
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
        description.appendText("File descriptor with name ").appendDescriptionOf(fileNameMatcher);
    }

    public static Matcher<? super FileDescriptor> fileDescriptorMatcher(String fileName) {
        return new FileDescriptorMatcher(CoreMatchers.equalTo(fileName));
    }
}
