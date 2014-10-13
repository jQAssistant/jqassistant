package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.*;

/**
 * Defines an abstract rule which is has an unique identifier and references a
 * Query.
 */
public abstract class AbstractRule implements Rule {

    /** Default severity level for concept. */
    public static Severity DEFAULT_CONCEPT_SEVERITY = Severity.MINOR;

    /** Default severity level for constraints. */
    public static Severity DEFAULT_CONSTRAINT_SEVERITY = Severity.INFO;

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

    /**
     * The severity of the constraint.
     */
    private Severity severity;

    public static <T extends Rule> Map<String,T> toMap(Collection<T> rules) {
        Map<String, T> result = new LinkedHashMap<>();
        for (T rule : rules) {
            result.put(rule.getId(),rule);
        }
        return result;
    }

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

    /**
     * Returns the severity.
     * 
     * @return {@link Severity}
     */
    public Severity getSeverity() {
        return severity;
    }

    /**
     * Sets the severity.
     * 
     * @param severity
     *            severity value
     */
    public void setSeverity(Severity severity) {
        this.severity = severity;
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
        return "AbstractRule [id=" + id + ", description=" + description + ", query=" + query + ", requiresConcepts=" + requiresConcepts + ", severity="
                + severity + "]";
    }

}
