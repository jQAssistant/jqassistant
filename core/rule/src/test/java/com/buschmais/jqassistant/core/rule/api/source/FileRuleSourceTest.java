package com.buschmais.jqassistant.core.rule.api.source;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import com.buschmais.jqassistant.core.rule.api.matcher.RuleSourceMatcher;
import com.buschmais.jqassistant.core.shared.mockito.MethodNotMockedAnswer;


public class FileRuleSourceTest {

    @Test
    public void knownRuleSourceTypesWillBeAddedWhenBuildingListOfRuleSources() throws Exception {
        File markDownFile = Mockito.mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(markDownFile).isFile();
        doReturn(false).when(markDownFile).isDirectory();
        doReturn("readme.md").when(markDownFile).getName();
        doReturn("/path/readme.md").when(markDownFile).getAbsolutePath();

        File xmlFile = Mockito.mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(xmlFile).isFile();
        doReturn(false).when(xmlFile).isDirectory();
        doReturn("rules.xml").when(xmlFile).getName();
        doReturn("/path/rules.xml").when(xmlFile).getAbsolutePath();

        File asciiDoctorFile = Mockito.mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(asciiDoctorFile).isFile();
        doReturn(false).when(asciiDoctorFile).isDirectory();
        doReturn("rules.adoc").when(asciiDoctorFile).getName();
        doReturn("/path/rules.adoc").when(asciiDoctorFile).getAbsolutePath();

        File ruleDirectory = mock(File.class, new MethodNotMockedAnswer());

        doReturn(new File[]{asciiDoctorFile, xmlFile, markDownFile}).when(ruleDirectory).listFiles();
        doReturn(true).when(ruleDirectory).isDirectory();
        doReturn(false).when(ruleDirectory).isFile();

        List<RuleSource> sources = FileRuleSource.getRuleSources(ruleDirectory);

        assertThat(sources, Matchers.hasSize(2));
        MatcherAssert.assertThat(sources, not(Matchers.hasItem(RuleSourceMatcher.matchesById("/path/readme.md"))));
        MatcherAssert.assertThat(sources, Matchers.hasItem(RuleSourceMatcher.matchesById("/path/rules.xml")));
        MatcherAssert.assertThat(sources, Matchers.hasItem(RuleSourceMatcher.matchesById("/path/rules.adoc")));
    }

    @Test
    public void nonRuleSourceFileWillBeIgnoredWhenBuildingListOfRuleSources() throws IOException {
        File markDownFile = Mockito.mock(File.class, new MethodNotMockedAnswer());
        File ruleDirectory = mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(markDownFile).isFile();
        doReturn(false).when(markDownFile).isDirectory();
        doReturn("readme.md").when(markDownFile).getName();


        doReturn(new File[] {markDownFile}).when(ruleDirectory).listFiles();
        doReturn(true).when(ruleDirectory).isDirectory();
        doReturn(false).when(ruleDirectory).isFile();

        List<RuleSource> sources = FileRuleSource.getRuleSources(ruleDirectory);

        assertThat(sources, Matchers.empty());
    }

    @Test
    public void asciiDoctorFileWillBeAddedWhenBuildingListOfRuleSources() throws IOException {
        File asciiDoctorFile = Mockito.mock(File.class, new MethodNotMockedAnswer());
        File ruleDirectory = mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(asciiDoctorFile).isFile();
        doReturn(false).when(asciiDoctorFile).isDirectory();
        doReturn("rules.adoc").when(asciiDoctorFile).getName();
        doReturn("/path/rules.adoc").when(asciiDoctorFile).getAbsolutePath();

        doReturn(new File[]{asciiDoctorFile}).when(ruleDirectory).listFiles();
        doReturn(true).when(ruleDirectory).isDirectory();
        doReturn(false).when(ruleDirectory).isFile();

        List<RuleSource> sources = FileRuleSource.getRuleSources(ruleDirectory);

        assertThat(sources, hasSize(1));
        MatcherAssert.assertThat(sources, Matchers.contains(RuleSourceMatcher.matchesById("/path/rules.adoc")));
    }

    @Test
    public void xmlFileWillBeAddedWhenBuildingListOfRuleSources() throws IOException {
        File xmlFile = Mockito.mock(File.class, new MethodNotMockedAnswer());
        File ruleDirectory = mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(xmlFile).isFile();
        doReturn(false).when(xmlFile).isDirectory();
        doReturn("rules.xml").when(xmlFile).getName();
        doReturn("/path/rules.xml").when(xmlFile).getAbsolutePath();

        doReturn(new File[]{xmlFile}).when(ruleDirectory).listFiles();
        doReturn(true).when(ruleDirectory).isDirectory();
        doReturn(false).when(ruleDirectory).isFile();

        List<RuleSource> sources = FileRuleSource.getRuleSources(ruleDirectory);

        assertThat(sources, hasSize(1));
        MatcherAssert.assertThat(sources, Matchers.contains(RuleSourceMatcher.matchesById("/path/rules.xml")));
    }
}