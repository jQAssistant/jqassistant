package com.buschmais.jqassistant.core.rule.api.source;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.core.shared.mockito.MethodNotMockedAnswer;

import org.hamcrest.Matchers;
import org.junit.Test;

import static com.buschmais.jqassistant.core.rule.api.matcher.RuleSourceMatcher.matchesById;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;


public class FileRuleSourceTest {

    @Test
    public void knownRuleSourceTypesWillBeAddedWhenBuildingListOfRuleSources() throws Exception {
        File markDownFile = mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(markDownFile).isFile();
        doReturn(false).when(markDownFile).isDirectory();
        doReturn("readme.md").when(markDownFile).getName();
        doReturn("/path/readme.md").when(markDownFile).getAbsolutePath();

        File xmlFile = mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(xmlFile).isFile();
        doReturn(false).when(xmlFile).isDirectory();
        doReturn("rules.xml").when(xmlFile).getName();
        doReturn("/path/rules.xml").when(xmlFile).getAbsolutePath();

        File asciiDoctorFile = mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(asciiDoctorFile).isFile();
        doReturn(false).when(asciiDoctorFile).isDirectory();
        doReturn("rules.adoc").when(asciiDoctorFile).getName();
        doReturn("/path/rules.adoc").when(asciiDoctorFile).getAbsolutePath();

        File ruleDirectory = mock(File.class, new MethodNotMockedAnswer());

        doReturn(new File[]{asciiDoctorFile, xmlFile, markDownFile}).when(ruleDirectory).listFiles();
        doReturn(true).when(ruleDirectory).isDirectory();
        doReturn(false).when(ruleDirectory).isFile();

        List<RuleSource> sources = FileRuleSource.getRuleSources(ruleDirectory);

        assertThat(sources, hasSize(3));
        assertThat(sources, hasItem(matchesById("/path/readme.md")));
        assertThat(sources, hasItem(matchesById("/path/rules.xml")));
        assertThat(sources, hasItem(matchesById("/path/rules.adoc")));
    }

    @Test
    public void nonRuleSourceFileWillBeIgnoredWhenBuildingListOfRuleSources() throws IOException {
        File markDownFile = mock(File.class, new MethodNotMockedAnswer());
        File ruleDirectory = mock(File.class, new MethodNotMockedAnswer());

        doReturn(true).when(markDownFile).isFile();
        doReturn(false).when(markDownFile).isDirectory();
        doReturn("readme.md").when(markDownFile).getName();


        doReturn(new File[] {markDownFile}).when(ruleDirectory).listFiles();
        doReturn(true).when(ruleDirectory).isDirectory();
        doReturn(false).when(ruleDirectory).isFile();

        List<RuleSource> sources = FileRuleSource.getRuleSources(ruleDirectory);

        assertThat(sources.size(), equalTo(1));
    }

    @Test
    public void asciiDoctorFileWillBeAddedWhenBuildingListOfRuleSources() throws IOException {
        File asciiDoctorFile = mock(File.class, new MethodNotMockedAnswer());
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
        assertThat(sources, Matchers.contains(matchesById("/path/rules.adoc")));
    }

    @Test
    public void xmlFileWillBeAddedWhenBuildingListOfRuleSources() throws IOException {
        File xmlFile = mock(File.class, new MethodNotMockedAnswer());
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
        assertThat(sources, Matchers.contains(matchesById("/path/rules.xml")));
    }
}
