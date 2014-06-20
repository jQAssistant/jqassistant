package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashSet;
import java.util.Set;

/**
 * Defines an abstract rule which is has an unique identifier and references a
 * Query.
 */
public abstract class AbstractRule implements Rule {

    /**
     * The id of the rule.
     */
    private String id;

    /**
     * The optional description.
     */
    private String description;

    /**
     * The cypher query which represents this rule.
     */
    private Query query;

    /**
     * The concepts which must be applied before this rule can be executed.
     */
    private Set<Concept> requiresConcepts = new HashSet<>();

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

    public Set<Concept> getRequiresConcepts() {
        return requiresConcepts;
    }

    public void setRequiresConcepts(Set<Concept> requiresConcepts) {
        this.requiresConcepts = requiresConcepts;
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
        AbstractRule executable = (AbstractRule) o;
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
        return "AbstractRule {" + "id='" + id + '\'' + ", description='" + description + '\'' + ", query=" + query + ", requiresConcepts=" + requiresConcepts
                + '}';
    }
}
