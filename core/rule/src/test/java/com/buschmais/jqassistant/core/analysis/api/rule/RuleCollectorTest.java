package com.buschmais.jqassistant.core.analysis.api.rule;

import java.io.File;

import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleCollector;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;

import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RuleCollectorTest {

    @Test
    public void testReadCompoundSources() throws Exception {
        File adocFile = ClasspathResource.getFile("/junit-without-assert.adoc");
        File xmlFile = ClasspathResource.getFile("/test-concepts.xml");
        RuleCollector ruleCollector = new RuleCollector(RuleConfiguration.builder().build());
        RuleSet ruleSet = ruleCollector.read(asList(new FileRuleSource(adocFile), new FileRuleSource(xmlFile)));
        assertThat(ruleSet.getConceptBucket().size(), equalTo(3));
        assertThat(ruleSet.getConstraintBucket().size(), equalTo(2));

        assertThat(ruleSet.getConceptBucket().getIds(), containsInAnyOrder("junit4:TestClassOrMethod",
                                                                           "junit4:AssertMethod",
                                                                           "java:Throwable"));
        assertThat(ruleSet.getConstraintBucket().getIds(), containsInAnyOrder("junit4:TestMethodWithoutAssertion",
                                                                              "example:ConstructorOfDateMustNotBeUsed"));

        assertThat(ruleSet.getGroupsBucket().size(), equalTo(1));

        Group group = ruleSet.getGroupsBucket().getById("default");

        assertThat(group.getId(), equalTo("default"));
        assertThat(group.getConcepts(), aMapWithSize(1));
        assertThat(group.getConcepts(), hasKey("java:Throwable"));
        assertThat(group.getConstraints().size(), equalTo(1));
        assertThat(group.getConstraints(), hasKey("example:ConstructorOfDateMustNotBeUsed"));
    }
}
