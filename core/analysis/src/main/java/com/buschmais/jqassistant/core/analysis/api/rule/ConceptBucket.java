package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

public class ConceptBucket {
    TreeMap<String, Concept> concepts = new TreeMap<>();

    public int size() {
        return concepts.size();
    }

    public void addConcept(Concept concept) throws DuplicateConceptException {
        if (concepts.containsKey(concept.getId())) {
            throw new DuplicateConceptException("The concept " + concept.getId() + " is already contained in this concept bucket");
        } else {
            concepts.put(concept.getId(), concept);
        }
    }

    /**
     * Returns a unmodifiable set with all concept ids.
     *
     * @return a set with all concept ids. Result will never be {@code null}.
     */
    public Set<String> getConceptIds() {
        return Collections.unmodifiableSet(concepts.keySet());
    }

    public Concept getConcept(String id) throws NoConceptException {
        Concept concept = concepts.get(id);

        if (null == concept) {
            throw new NoConceptException(id);
        }

        return concept;
    }

    public void addConcepts(ConceptBucket bucket) throws DuplicateConceptException {
        String id = null;
        try {
            for (String conceptId : bucket.getConceptIds()) {
                id = conceptId;
                Concept concept = null;
                concept = bucket.getConcept(id);
                addConcept(concept);
            }
        } catch (NoConceptException e) {
            throw new IllegalStateException("Concept " + id + " not found in overhanded bucket. The bucket is in an inconsistent state.");
        }

    }
}
