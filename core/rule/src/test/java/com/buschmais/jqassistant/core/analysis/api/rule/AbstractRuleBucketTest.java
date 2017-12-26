package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class AbstractRuleBucketTest {

    private TestBucket bucket;

    @Before
    public void setUp() {
        bucket = new TestBucket();
    }


    //--- All tests for getRules()

    @Test
    public void getRulesReturnsAllRules() throws DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        bucket.add(a);
        bucket.add(b);
        bucket.add(c);

        assertThat(bucket.getAll(), containsInAnyOrder(a, b, c));
    }


    //--- All tests for getIds()

    @Test
    public void getConceptsIdsReturnsEmptySetIfThereAreNoConceptsInTheBucket() {
        assertThat(bucket.getIds(), Matchers.<String>empty());
    }

    @Test
    public void getConceptIdsReturnsAllIdsOfAllConceptsInBucket() throws DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        bucket.add(a);
        bucket.add(b);
        bucket.add(c);

        assertThat(bucket.getIds(), hasSize(3));
        assertThat(bucket.getIds(), containsInAnyOrder("a", "b", "c"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getConceptIdsReturnsUnmodifiableSet() {
        Set<String> conceptIds = bucket.getIds();

        conceptIds.add("a");
    }

    //--- All tests for size()

    @Test
    public void sizeOfBucketIsZeroIfThereAreNotConcepts() {
        assertThat(bucket.size(), equalTo(0));
    }

    @Test
    public void sizeOfBucketIsEqualNumberOfConceptsInBucket() throws DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        bucket.add(a);
        bucket.add(b);
        bucket.add(c);

        assertThat(bucket.size(), equalTo(3));
    }

    //--- All tests for getConcept()

    @Test
    public void getConceptReturnsExistingConceptInBucket() throws NoConceptException, DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        bucket.add(a);

        Concept b = bucket.getById("a");

        assertThat(b, Matchers.sameInstance(a));
    }

    @Test(expected = NoConceptException.class)
    public void getConceptThrowsExceptionIfConceptNotFoundInBucket() throws NoConceptException {
        bucket.getById("foobar");
    }

    //--- All tests for addConcepts

    @Test()
    public void addConceptsAddsAllConcepts() throws NoConceptException, DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

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
    public void addConceptsCopesWithEmptyBucket() throws NoConceptException, DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        TestBucket newBucket = new TestBucket();

        newBucket.add(a);
        newBucket.add(b);
        newBucket.add(c);
        newBucket.add(new TestBucket());

        assertThat(newBucket.size(), equalTo(3));
        assertThat(newBucket.getIds(), containsInAnyOrder("a", "b", "c"));
    }

    @Test(expected = DuplicateConceptException.class)
    public void addWithCollectionFailIfAConceptIdIsSameConceptIdIsAlreadyInBucket() throws DuplicateConceptException, NoConceptException {
        Concept first = Mockito.mock(Concept.class);
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(first.getId()).thenReturn("a");
        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        TestBucket existingBucket = new TestBucket();

        existingBucket.add(a);
        existingBucket.add(b);
        existingBucket.add(c);

        TestBucket newBucket = new TestBucket();

        newBucket.add(first);
        newBucket.add(existingBucket);
    }


    //--- Helper Classes
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