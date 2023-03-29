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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
        assertThat(ruleSet.getConceptBucket().size(), equalTo(1));
        assertThat(ruleSet.getConstraintBucket().size(), equalTo(1));

        assertThat(ruleSet.getConceptBucket().getIds(), containsInAnyOrder("java:Throwable"));
        assertThat(ruleSet.getConstraintBucket().getIds(), containsInAnyOrder("example:ConstructorOfDateMustNotBeUsed"));

        assertThat(ruleSet.getGroupsBucket().size(), equalTo(1));

        Group group = ruleSet.getGroupsBucket().getById("default");

        assertThat(group.getId(), equalTo("default"));
        assertThat(group.getConcepts(), aMapWithSize(1));
        assertThat(group.getConcepts(), hasKey("java:Throwable"));
        assertThat(group.getConstraints().size(), equalTo(1));
        assertThat(group.getConstraints(), hasKey("example:ConstructorOfDateMustNotBeUsed"));
    }
}
