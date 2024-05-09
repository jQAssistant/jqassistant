package com.buschmais.jqassistant.core.rule.api.model;

import java.io.File;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.rule.impl.reader.XmlRuleParserPlugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RuleParserTest {

    private RuleParser ruleParser;

    @Mock
    private Rule rule;

    @BeforeEach
    void setUp() {
        XmlRuleParserPlugin xmlRuleParserPlugin = new XmlRuleParserPlugin();
        xmlRuleParserPlugin.initialize();
        xmlRuleParserPlugin.configure(rule);
        ruleParser = new RuleParser(asList(xmlRuleParserPlugin));
    }

    @Test
    void readRuleSet() throws Exception {
        File rulesDirectory = new File(RuleParserTest.class.getResource("/").getPath());
        RuleSet ruleSet = ruleParser
                .parse(asList(new FileRuleSource(rulesDirectory, "test-concepts.xml")));
        assertThat(ruleSet.getConceptBucket().size()).isEqualTo(1);
        assertThat(ruleSet.getConstraintBucket().size()).isEqualTo(1);

        assertThat(ruleSet.getConceptBucket().getIds()).containsExactlyInAnyOrder("java:Throwable");
        assertThat(ruleSet.getConstraintBucket().getIds()).containsExactlyInAnyOrder("example:ConstructorOfDateMustNotBeUsed");

        assertThat(ruleSet.getGroupsBucket().size()).isEqualTo(1);

        Group group = ruleSet.getGroupsBucket().getById("default");

        assertThat(group.getId()).isEqualTo("default");
        assertThat(group.getConcepts()).hasSize(1);
        assertThat(group.getConcepts()).containsKey("java:Throwable");
        assertThat(group.getConstraints().size()).isEqualTo(1);
        assertThat(group.getConstraints()).containsKey("example:ConstructorOfDateMustNotBeUsed");
    }
}
