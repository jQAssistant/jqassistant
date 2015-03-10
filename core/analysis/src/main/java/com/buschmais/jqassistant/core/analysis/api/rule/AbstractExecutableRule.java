package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

/**
 * Defines an abstract rule which is has an unique identifier and references a
 * Query.
 */
public abstract class AbstractExecutableRule implements ExecutableRule {

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
     * The optional deprecation message.
     */
    private String deprecation;

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
    private Set<String> requiresConcepts;

    /**
     * Describes the verification of the result of an executable rule.
     */
    private Verification verification;

    /**
     * Constructor.
     *
     * @param id
     *            The id.
     * @param description
     *            The human readable description.
     * @param severity
     *            The severity.
     * @param deprecation
     *            The deprecation message.
     * @param cypher
     *            The cypher query.
     * @param templateId
     *            The query template.
     * @param parameters
     *            The parametes.
     * @param requiresConcepts
     *            The required concept ids.
     * @param verification
     *            The result verification.
     */
    protected AbstractExecutableRule(String id, String description, Severity severity, String deprecation, String cypher, Script script, String templateId,
            Map<String, Object> parameters, Set<String> requiresConcepts, Verification verification) {
        this.id = id;
        this.description = description;
        this.severity = severity;
        this.deprecation = deprecation;
        this.cypher = cypher;
        this.script = script;
        this.templateId = templateId;
        this.parameters = parameters;
        this.requiresConcepts = requiresConcepts;
        this.verification = verification;
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
    @Override
    public Severity getSeverity() {
        return severity;
    }

    @Override
    public String getDeprecation() {
        return deprecation;
    }

    @Override
    public Set<String> getRequiresConcepts() {
        return requiresConcepts;
    }

    @Override
    public String getCypher() {
        return cypher;
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public Verification getVerification() {
        return verification;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AbstractExecutableRule executable = (AbstractExecutableRule) o;
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
        return "AbstractExecutableRule [id=" + id + ", description=" + description + ", cypher=" + cypher + ", requiresConcepts=" + requiresConcepts
                + ", severity=" + severity + "]";
    }

}
