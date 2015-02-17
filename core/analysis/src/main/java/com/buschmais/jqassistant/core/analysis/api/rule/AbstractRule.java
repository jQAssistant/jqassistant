package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * The severity of the constraint.
     */
    private Severity severity;

    /**
     * The optional deprecated message.
     */
    private String deprecated;

    /**
     * The cypher query which represents this rule.
     */
    private String cypher;

    /**
     * The scropt which represents this rule;
     */
    private Script script;

    /**
     * The query template to use.
     */
    private String templateId;

    /**
     * The parameters to use.
     */
    private Map<String, Object> parameters;

    /**
     * The concepts which must be applied before this rule can be executed.
     */
    private Set<String> requiresConcepts = new HashSet<>();

    /**
     * Constructor.
     *
     * @param id
     *            The id.
     * @param description
     *            The human readable description.
     * @param severity
     *            The severity.
     * @param deprecated
     *            The deprecated message.
     * @param cypher
     *            The cypher query.
     * @param templateId
     *            The query template.
     * @param parameters
     *            The parametes.
     * @param requiresConcepts
     *            The required concept ids.
     */
    protected AbstractRule(String id, String description, Severity severity, String deprecated, String cypher, Script script, String templateId,
            Map<String, Object> parameters, Set<String> requiresConcepts) {
        this.id = id;
        this.description = description;
        this.severity = severity;
        this.deprecated = deprecated;
        this.cypher = cypher;
        this.script = script;
        this.templateId = templateId;
        this.parameters = parameters;
        this.requiresConcepts = requiresConcepts;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the severity.
     *
     * @return {@link Severity}
     */
    public Severity getSeverity() {
        return severity;
    }

    public String getDeprecated() {
        return deprecated;
    }

    public Set<String> getRequiresConcepts() {
        return requiresConcepts;
    }

    public String getCypher() {
        return cypher;
    }

    public Script getScript() {
        return script;
    }

    public String getTemplateId() {
        return templateId;
    }

    public Map<String, Object> getParameters() {
        return parameters;
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
        return "AbstractRule [id=" + id + ", description=" + description + ", cypher=" + cypher + ", requiresConcepts=" + requiresConcepts + ", severity="
                + severity + "]";
    }

}
