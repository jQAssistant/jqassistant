package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines an executable which is has an unique identifier and references a
 * Query.
 */
public class AbstractExecutable implements Rule {

    private String id;

    private String description;

    private Query query;

    private Set<Concept> requiredConcepts = new HashSet<Concept>();

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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbstractExecutable executable = (AbstractExecutable) o;
        if (!id.equals(executable.id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "AbstractExecutable{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", query=" + query +
                ", requiredConcepts=" + requiredConcepts +
                '}';
    }
}
