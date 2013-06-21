package com.buschmais.jqassistant.core.analysis.api.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: dimahler
 * Date: 6/21/13
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Concept {

    private String id;

    private String description;

    private Set<Concept> requiredConcepts = new HashSet<Concept>();

    private Query query;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Concept> getRequiredConcepts() {
        return requiredConcepts;
    }

    public void setRequiredConcepts(Set<Concept> requiredConcepts) {
        this.requiredConcepts = requiredConcepts;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }
}
