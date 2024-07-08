package com.buschmais.jqassistant.core.rule.api.source;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class ClasspathRuleSourceTest extends AbstractRuleSourceTest {

    @Override
    protected List<RuleSource> getRuleSources() {
        return Stream.of("rules.xml", "index.adoc", "readme.md", "subdirectory/rules.xml")
                .map(relativePath -> new ClasspathRuleSource(ClasspathRuleSourceTest.class.getClassLoader(), relativePath)).collect(toList());
    }

}
