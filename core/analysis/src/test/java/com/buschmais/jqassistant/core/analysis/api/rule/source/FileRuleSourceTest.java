package com.buschmais.jqassistant.core.analysis.api.rule.source;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import static com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSourceTest.RuleSourceMatcher.matchesById;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.*;


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
        assertThat(sources, not(hasItem(matchesById("/path/readme.md"))));
        assertThat(sources, hasItem(matchesById("/path/rules.xml")));
        assertThat(sources, hasItem(matchesById("/path/rules.adoc")));
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
        assertThat(sources, contains(matchesById("/path/rules.adoc")));
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
        assertThat(sources, Matchers.contains(matchesById("/path/rules.xml")));
    }


    public static class MethodNotMockedAnswer implements Answer {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Method calledMethod = invocation.getMethod();
            String signature = calledMethod.toGenericString();

            throw new RuntimeException(signature + " is not mocked!");
        }
    }

    public static class RuleSourceMatcher extends TypeSafeMatcher<RuleSource> {

        private final String ruleSourceId;

        private RuleSourceMatcher(String id) {
            ruleSourceId = id;
        }

        @Override
        protected boolean matchesSafely(RuleSource ruleSource) {
            return ruleSourceId.equals(ruleSource.getId());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(ruleSourceId);
        }

        @Override
        protected void describeMismatchSafely(RuleSource item, Description mismatchDescription) {
            mismatchDescription.appendText(item.getId());
        }

        public static Matcher<RuleSource> matchesById(String id) {
            return new RuleSourceMatcher(id);
        }
    }
}