package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FilePatternMatcherTest {

    private FilePatternMatcher filePatternMatcher;

    @Test
    public void includeSingleFilePattern() throws IOException {
        configure("*.xml", null);
        assertThat(filePatternMatcher.accepts("test.xml")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("test.txt")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("/META-INF/persistence.xml")).isEqualTo(true);
    }

    @Test
    void excludeSingleFilePattern() throws IOException {
        configure(null, "*.xml");
        assertThat(filePatternMatcher.accepts("test.xml")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test.txt")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("/META-INF/persistence.xml")).isEqualTo(false);
    }

    @Test
    void includeAndExcludeSingleFilePatterns() throws IOException {
        configure("test.*", "*.xml");
        assertThat(filePatternMatcher.accepts("test.xml")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test.txt")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("other.txt")).isEqualTo(false);
    }

    @Test
    void includeMultipleFilePatterns() throws IOException {
        configure("*.xml, *.xsd", null);
        assertThat(filePatternMatcher.accepts("test.xml")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("test.xsd")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("test.txt")).isEqualTo(false);
    }

    @Test
    void excludeMultipleFilePatterns() throws IOException {
        configure(null, "*.xml, *.xsd");
        assertThat(filePatternMatcher.accepts("test.xml")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test.xsd")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test.txt")).isEqualTo(true);
    }

    @Test
    void includeAndExcludeMultipleFilePatterns() throws IOException {
        configure("test1.*,test2.*", "*.xml, *.xsd");
        assertThat(filePatternMatcher.accepts("test1.xml")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test2.xml")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test1.xsd")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test2.xsd")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test1.txt")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("test2.txt")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("other.txt")).isEqualTo(false);
    }

    @Test
    void includeMultipleFilePatternsWithFolder() throws IOException {
        configure("/META-INF/*.xml,/WEB-INF/*.xml", null);
        assertThat(filePatternMatcher.accepts("/META-INF/persistence.xml")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("/WEB-INF/persistence.xml")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("test.xml")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test.txt")).isEqualTo(false);
    }

    @Test
    void excludeMultipleFilePatternsWithFolder() throws IOException {
        configure(null, "/META-INF/*.xml,/WEB-INF/*.xml");
        assertThat(filePatternMatcher.accepts("/META-INF/persistence.xml")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("/WEB-INF/persistence.xml")).isEqualTo(false);
        assertThat(filePatternMatcher.accepts("test.xml")).isEqualTo(true);
        assertThat(filePatternMatcher.accepts("test.txt")).isEqualTo(true);
    }

    private void configure(String includes, String excludes) {
        filePatternMatcher = FilePatternMatcher.builder().include(includes).exclude(excludes).build();
    }
}
