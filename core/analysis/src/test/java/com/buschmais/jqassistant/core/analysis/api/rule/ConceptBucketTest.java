package com.buschmais.jqassistant.core.analysis.api.rule;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.asList;
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

        TreeSet<String> ids = new TreeSet<>(asList(a.getId(), b.getId(), c.getId()));

        ConceptBucket existingBucket = Mockito.mock(ConceptBucket.class);

        Mockito.when(existingBucket.getConceptIds()).thenReturn(ids);
        Mockito.when(existingBucket.getConcept(Mockito.eq("a"))).thenReturn(a);
        Mockito.when(existingBucket.getConcept(Mockito.eq("b"))).thenReturn(b);
        Mockito.when(existingBucket.getConcept(Mockito.eq("c"))).thenReturn(c);

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

        TreeSet<String> ids = new TreeSet<>(asList(a.getId(), b.getId(), c.getId()));

        ConceptBucket existingBucket = Mockito.mock(ConceptBucket.class);

        Mockito.when(existingBucket.getConceptIds()).thenReturn(ids);
        Mockito.when(existingBucket.getConcept(Mockito.eq("a"))).thenReturn(a);
        Mockito.when(existingBucket.getConcept(Mockito.eq("b"))).thenReturn(b);
        Mockito.when(existingBucket.getConcept(Mockito.eq("c"))).thenReturn(c);

        ConceptBucket newBucket = new ConceptBucket();

        newBucket.addConcept(first);
        newBucket.addConcepts(existingBucket);
    }

}