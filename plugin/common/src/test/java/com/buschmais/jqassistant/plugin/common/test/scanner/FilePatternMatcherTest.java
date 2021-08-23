package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class FilePatternMatcherTest {

    private FilePatternMatcher filePatternMatcher;

    @Test
    public void includeSingleFilePattern() throws IOException {
        configure("*.xml", null);
        assertThat(filePatternMatcher.accepts("test.xml"), equalTo(true));
        assertThat(filePatternMatcher.accepts("test.txt"), equalTo(false));
        assertThat(filePatternMatcher.accepts("/META-INF/persistence.xml"), equalTo(true));
    }

    @Test
    void excludeSingleFilePattern() throws IOException {
        configure(null, "*.xml");
        assertThat(filePatternMatcher.accepts("test.xml"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test.txt"), equalTo(true));
        assertThat(filePatternMatcher.accepts("/META-INF/persistence.xml"), equalTo(false));
    }

    @Test
    void includeAndExcludeSingleFilePatterns() throws IOException {
        configure("test.*", "*.xml");
        assertThat(filePatternMatcher.accepts("test.xml"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test.txt"), equalTo(true));
        assertThat(filePatternMatcher.accepts("other.txt"), equalTo(false));
    }

    @Test
    void includeMultipleFilePatterns() throws IOException {
        configure("*.xml, *.xsd", null);
        assertThat(filePatternMatcher.accepts("test.xml"), equalTo(true));
        assertThat(filePatternMatcher.accepts("test.xsd"), equalTo(true));
        assertThat(filePatternMatcher.accepts("test.txt"), equalTo(false));
    }

    @Test
    void excludeMultipleFilePatterns() throws IOException {
        configure(null, "*.xml, *.xsd");
        assertThat(filePatternMatcher.accepts("test.xml"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test.xsd"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test.txt"), equalTo(true));
    }

    @Test
    void includeAndExcludeMultipleFilePatterns() throws IOException {
        configure("test1.*,test2.*", "*.xml, *.xsd");
        assertThat(filePatternMatcher.accepts("test1.xml"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test2.xml"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test1.xsd"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test2.xsd"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test1.txt"), equalTo(true));
        assertThat(filePatternMatcher.accepts("test2.txt"), equalTo(true));
        assertThat(filePatternMatcher.accepts("other.txt"), equalTo(false));
    }

    @Test
    void includeMultipleFilePatternsWithFolder() throws IOException {
        configure("/META-INF/*.xml,/WEB-INF/*.xml", null);
        assertThat(filePatternMatcher.accepts("/META-INF/persistence.xml"), equalTo(true));
        assertThat(filePatternMatcher.accepts("/WEB-INF/persistence.xml"), equalTo(true));
        assertThat(filePatternMatcher.accepts("test.xml"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test.txt"), equalTo(false));
    }

    @Test
    void excludeMultipleFilePatternsWithFolder() throws IOException {
        configure(null, "/META-INF/*.xml,/WEB-INF/*.xml");
        assertThat(filePatternMatcher.accepts("/META-INF/persistence.xml"), equalTo(false));
        assertThat(filePatternMatcher.accepts("/WEB-INF/persistence.xml"), equalTo(false));
        assertThat(filePatternMatcher.accepts("test.xml"), equalTo(true));
        assertThat(filePatternMatcher.accepts("test.txt"), equalTo(true));
    }

    private void configure(String includes, String excludes) {
        filePatternMatcher = FilePatternMatcher.builder().include(includes).exclude(excludes).build();
    }
}
