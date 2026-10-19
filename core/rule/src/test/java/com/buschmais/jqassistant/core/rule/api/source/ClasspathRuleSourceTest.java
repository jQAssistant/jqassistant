package com.buschmais.jqassistant.core.rule.api.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ClasspathRuleSourceTest extends AbstractRuleSourceTest {

    private static URL pluginBaseURL;

    @BeforeAll
    static void setUp() throws MalformedURLException {
        String url = ClasspathRuleSourceTest.class.getClassLoader()
            .getResource(".plugin-base-marker")
            .toString();
        pluginBaseURL = new URL(url.substring(0, url.lastIndexOf('/')) + "/");
    }

    @Override
    protected List<RuleSource> getRuleSources() {
        return Stream.of("rules.xml", "index.adoc", "readme.md", "subdirectory/rules.xml")
            .map(relativePath -> new ClasspathRuleSource(pluginBaseURL, relativePath))
            .collect(toList());
    }

    @Test
    void nonExistingResource() {
        assertThatExceptionOfType(IOException.class).isThrownBy(() -> new ClasspathRuleSource(pluginBaseURL, "non-existing.xml").getInputStream());
    }

}
