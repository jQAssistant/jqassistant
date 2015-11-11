package com.buschmais.jqassistant.core.analysis.api.rule;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.*;

public class ConceptBucketTest {
    private ConceptBucket concepts;

    @Before
    public void setUp() {
        concepts = new ConceptBucket();
    }

    @Test
    public void sizeOfBucketIsZeroIfThereAreNotConcepts() {
        assertThat(concepts.size(), equalTo(0));
    }

    //--- All tests for getConcpets()

    @Test
    public void getConceptsReturnsAllConcepts() throws DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        concepts.addConcept(a);
        concepts.addConcept(b);
        concepts.addConcept(c);

        assertThat(concepts.getConcepts(), containsInAnyOrder(a, b, c));
    }

    //--- All tests for size()

    @Test
    public void sizeOfBucketIsEqualNumberOfConceptsInBucket() throws DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        concepts.addConcept(a);
        concepts.addConcept(b);
        concepts.addConcept(c);

        assertThat(concepts.size(), equalTo(3));
    }

    //--- All tests for getConceptIds()

    @Test
    public void getConceptsIdsReturnsEmptySetIfThereAreNoConceptsInTheBucket() {
        assertThat(concepts.getConceptIds(), Matchers.<String>empty());
    }

    @Test
    public void getConceptIdsReturnsAllIdsOfAllConceptsInBucket() throws DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        concepts.addConcept(a);
        concepts.addConcept(b);
        concepts.addConcept(c);

        assertThat(concepts.getConceptIds(), hasSize(3));
        assertThat(concepts.getConceptIds(), containsInAnyOrder("a", "b", "c"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getConceptIdsReturnsUnmodifiableSet() {
        Set<String> conceptIds = concepts.getConceptIds();

        conceptIds.add("a");
    }

    //--- All tests for getConcept()

    @Test
    public void getConceptReturnsExistingConceptInBucket() throws NoConceptException, DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        concepts.addConcept(a);

        Concept b = concepts.getConcept("a");

        assertThat(b, Matchers.sameInstance(a));
    }

    @Test(expected = NoConceptException.class)
    public void getConceptThrowsExceptionIfConceptNotFoundInBucket() throws NoConceptException {
        concepts.getConcept("foobar");
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

        ConceptBucket existingBucket = new ConceptBucket();

        existingBucket.addConcept(a);
        existingBucket.addConcept(b);
        existingBucket.addConcept(c);

        ConceptBucket newBucket = new ConceptBucket();

        newBucket.addConcepts(existingBucket);

        assertThat(newBucket.size(), equalTo(3));
        assertThat(newBucket.getConceptIds(), containsInAnyOrder("a", "b", "c"));
    }

    @Test
    public void addConceptsCopesWithEmptyBucket() throws NoConceptException, DuplicateConceptException {
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        ConceptBucket newBucket = new ConceptBucket();

        newBucket.addConcept(a);
        newBucket.addConcept(b);
        newBucket.addConcept(c);
        newBucket.addConcepts(new ConceptBucket());

        assertThat(newBucket.size(), equalTo(3));
        assertThat(newBucket.getConceptIds(), containsInAnyOrder("a", "b", "c"));
    }

    @Test(expected = DuplicateConceptException.class)
    public void addConceptsFailIfAConceptIdIsSameConceptIdIsAreadyInBucket() throws DuplicateConceptException, NoConceptException {
        Concept first = Mockito.mock(Concept.class);
        Concept a = Mockito.mock(Concept.class);
        Concept b = Mockito.mock(Concept.class);
        Concept c = Mockito.mock(Concept.class);

        Mockito.when(first.getId()).thenReturn("a");
        Mockito.when(a.getId()).thenReturn("a");
        Mockito.when(b.getId()).thenReturn("b");
        Mockito.when(c.getId()).thenReturn("c");

        ConceptBucket existingBucket = new ConceptBucket();

        existingBucket.addConcept(a);
        existingBucket.addConcept(b);
        existingBucket.addConcept(c);

        ConceptBucket newBucket = new ConceptBucket();

        newBucket.addConcept(first);
        newBucket.addConcepts(existingBucket);
    }

}