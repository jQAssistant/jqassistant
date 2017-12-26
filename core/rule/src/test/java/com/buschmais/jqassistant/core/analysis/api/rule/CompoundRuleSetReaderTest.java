package com.buschmais.jqassistant.core.analysis.api.rule;

import java.io.File;
import java.net.URL;

import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSetReader;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.UrlRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.rule.impl.reader.XmlRuleSetReader;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CompoundRuleSetReaderTest {

    @Test
    public void testReadCompoundSources() throws Exception {
        File adocFile = ClasspathResource.getFile("/junit-without-assert.adoc");
        File xmlFile = ClasspathResource.getFile("/test-concepts.xml");
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        RuleSetReader reader = new CompoundRuleSetReader(RuleConfiguration.builder().build());
        reader.read(asList(new FileRuleSource(adocFile), new FileRuleSource(xmlFile)), ruleSetBuilder);

        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
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

    @Test
    public void testReadUrlSource() throws Exception {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        URL url = getClass().getResource("/test-concepts.xml");
        RuleSetReader reader = new XmlRuleSetReader(RuleConfiguration.builder().build());
        reader.read(singletonList(new UrlRuleSource(url)), ruleSetBuilder);

        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
        assertThat(ruleSet.getConceptBucket().size(), equalTo(1));
        assertThat(ruleSet.getConstraintBucket().size(), equalTo(1));
        assertThat(ruleSet.getConceptBucket().getIds(), contains("java:Throwable"));
        assertThat(ruleSet.getConstraintBucket().getIds(), contains("example:ConstructorOfDateMustNotBeUsed"));
        assertThat(ruleSet.getGroupsBucket().size(), equalTo(1));

        Group group = ruleSet.getGroupsBucket().getById("default");
        assertThat(group.getId(), equalTo("default"));
    }
}
