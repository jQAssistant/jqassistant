package com.buschmais.jqassistant.core.rule.api.model;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractRuleBucketTest {

    private TestBucket bucket = new TestBucket();

    // --- All tests for getRules()

    @Test
    public void getRulesReturnsAllRules() throws DuplicateConceptException {
        Concept a = mock(Concept.class);
        Concept b = mock(Concept.class);
        Concept c = mock(Concept.class);

        when(a.getId()).thenReturn("a");
        when(b.getId()).thenReturn("b");
        when(c.getId()).thenReturn("c");

        bucket.add(a);
        bucket.add(b);
        bucket.add(c);

        assertThat(bucket.getAll(), containsInAnyOrder(a, b, c));
    }

    // --- All tests for getIds()

    @Test
    public void getConceptsIdsReturnsEmptySetIfThereAreNoConceptsInTheBucket() {
        assertThat(bucket.getIds(), Matchers.<String> empty());
    }

    @Test
    public void getConceptIdsReturnsAllIdsOfAllConceptsInBucket() throws DuplicateConceptException {
        Concept a = mock(Concept.class);
        Concept b = mock(Concept.class);
        Concept c = mock(Concept.class);

        when(a.getId()).thenReturn("a");
        when(b.getId()).thenReturn("b");
        when(c.getId()).thenReturn("c");

        bucket.add(a);
        bucket.add(b);
        bucket.add(c);

        assertThat(bucket.getIds(), hasSize(3));
        assertThat(bucket.getIds(), containsInAnyOrder("a", "b", "c"));
    }

    @Test
    public void getConceptIdsReturnsUnmodifiableSet() {
        Set<String> conceptIds = bucket.getIds();

        Assertions.assertThatThrownBy(() -> conceptIds.add("a"))
                  .isInstanceOf(UnsupportedOperationException.class);
    }

    // --- All tests for size()

    @Test
    public void sizeOfBucketIsZeroIfThereAreNotConcepts() {
        assertThat(bucket.size()).isEqualTo(0);
    }

    @Test
    public void sizeOfBucketIsEqualNumberOfConceptsInBucket() throws DuplicateConceptException {
        Concept a = mock(Concept.class);
        Concept b = mock(Concept.class);
        Concept c = mock(Concept.class);

        when(a.getId()).thenReturn("a");
        when(b.getId()).thenReturn("b");
        when(c.getId()).thenReturn("c");

        bucket.add(a);
        bucket.add(b);
        bucket.add(c);

        assertThat(bucket.size(), equalTo(3));
    }

    // --- All tests for getConcept()

    @Test
    public void getConceptReturnsExistingConceptInBucket() throws NoConceptException, DuplicateConceptException {
        Concept a = mock(Concept.class);

        when(a.getId()).thenReturn("a");
        bucket.add(a);

        Concept b = bucket.getById("a");

        assertThat(b, Matchers.sameInstance(a));
    }

    @Test
    public void getConceptThrowsExceptionIfConceptNotFoundInBucket() throws NoConceptException {
        Assertions.assertThatThrownBy(() -> bucket.getById("foobar"))
                  .isInstanceOf(NoConceptException.class);
    }

    // --- All tests for addConcepts

    @Test()
    public void addConceptsAddsAllConcepts() throws DuplicateConceptException {
        Concept a = mock(Concept.class);
        Concept b = mock(Concept.class);
        Concept c = mock(Concept.class);

        when(a.getId()).thenReturn("a");
        when(b.getId()).thenReturn("b");
        when(c.getId()).thenReturn("c");

        TestBucket existingBucket = new TestBucket();

        existingBucket.add(a);
        existingBucket.add(b);
        existingBucket.add(c);

        ConceptBucket newBucket = new ConceptBucket();

        newBucket.add(existingBucket);

        assertThat(newBucket.size(), equalTo(3));
        assertThat(newBucket.getIds(), containsInAnyOrder("a", "b", "c"));
    }

    @Test
    public void addConceptsCopesWithEmptyBucket() throws DuplicateConceptException {
        Concept a = mock(Concept.class);
        Concept b = mock(Concept.class);
        Concept c = mock(Concept.class);

        when(a.getId()).thenReturn("a");
        when(b.getId()).thenReturn("b");
        when(c.getId()).thenReturn("c");

        TestBucket newBucket = new TestBucket();

        newBucket.add(a);
        newBucket.add(b);
        newBucket.add(c);
        newBucket.add(new TestBucket());

        assertThat(newBucket.size(), equalTo(3));
        assertThat(newBucket.getIds(), containsInAnyOrder("a", "b", "c"));
    }

    @Test
    public void addWithCollectionFailIfAConceptIdIsSameConceptIdIsAlreadyInBucket() throws DuplicateConceptException {
        Concept first = mock(Concept.class);
        Concept a = mock(Concept.class);
        Concept b = mock(Concept.class);
        Concept c = mock(Concept.class);

        when(first.getId()).thenReturn("a");
        when(a.getId()).thenReturn("a");
        when(b.getId()).thenReturn("b");
        when(c.getId()).thenReturn("c");

        TestBucket existingBucket = new TestBucket();

        existingBucket.add(a);
        existingBucket.add(b);
        existingBucket.add(c);

        TestBucket newBucket = new TestBucket();

        newBucket.add(first);
        Assertions.assertThatThrownBy(() -> newBucket.add(existingBucket))
                  .isInstanceOf(DuplicateConceptException.class);
    }

    @Test
    public void match() throws DuplicateConceptException, NoRuleException {
        Concept c1 = mock(Concept.class);
        Concept c2 = mock(Concept.class);
        Concept c3 = mock(Concept.class);

        when(c1.getId()).thenReturn("concept1");
        when(c2.getId()).thenReturn("concept2");
        when(c3.getId()).thenReturn("concept3");

        TestBucket bucket = new TestBucket();
        bucket.add(c1);
        bucket.add(c2);
        bucket.add(c3);

        assertThat(bucket.match("concept1"), equalTo(singletonList(c1)));
        assertThat(bucket.match("c?ncept1"), equalTo(singletonList(c1)));
        assertThat(bucket.match("concept?"), equalTo(asList(c1, c2, c3)));
        assertThat(bucket.match("c*ncept1"), equalTo(singletonList(c1)));
        assertThat(bucket.match("concept*"), equalTo(asList(c1, c2, c3)));
        try {
            assertThat(bucket.match("custom-concept"), equalTo(emptyList()));
            fail("Expecting a " + NoConceptException.class.getName());
        } catch (NoConceptException e) {
            assertThat(e.getMessage(), containsString("custom-concept"));
        }
    }

    // --- Helper Classes
    private static class TestBucket extends AbstractRuleBucket<Concept, NoConceptException, DuplicateConceptException> {
        @Override
        protected String getRuleTypeName() {
            return "example";
        }

        @Override
        protected DuplicateConceptException newDuplicateRuleException(String message) {
            return new DuplicateConceptException(message);
        }

        @Override
        protected NoConceptException newNoRuleException(String message) {
            return new NoConceptException(message);
        }
    }
}
