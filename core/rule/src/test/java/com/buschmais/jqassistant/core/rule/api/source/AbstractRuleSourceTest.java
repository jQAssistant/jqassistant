package com.buschmais.jqassistant.core.rule.api.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractRuleSourceTest {

    @Test
    void classpathRuleSource() throws IOException {
        List<RuleSource> ruleSources = getRuleSources();

        Map<String, RuleSource> sources = ruleSources.stream().collect(toMap(k -> k.getRelativePath(), v -> v));
        assertThat(sources).hasSize(4);

        assertThat(sources.keySet()).containsExactlyInAnyOrder("rules.xml", "index.adoc", "readme.md", "subdirectory/rules.xml");

        for (RuleSource ruleSource : ruleSources) {
            try (InputStream inputStream = ruleSource.getInputStream()) {
                assertThat(inputStream).isNotNull();
            }
        }
    }

    protected abstract List<RuleSource> getRuleSources() throws IOException;

}
