package com.buschmais.jqassistant.core.report.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines the interface for report plugins.
 */
public interface ReportPlugin {

    @Retention(RUNTIME)
    @Target(TYPE)
    @interface Selectable {

    }
    /*
     * Initializes the plugin.
     */
    void initialize() throws ReportException;

    /**
     * Initializes the plugin with the given properties.
     *
     * @param reportContext
     *            The {@link ReportContext}.
     * @param properties
     *            The properties.
     * @throws ReportException
     *             If the plugin cannot be initialized.
     */
    void configure(ReportContext reportContext, Map<String, Object> properties) throws ReportException;

    void begin() throws ReportException;

    void end() throws ReportException;

    void beginConcept(Concept concept) throws ReportException;

    void endConcept() throws ReportException;

    void beginGroup(Group group) throws ReportException;

    void endGroup() throws ReportException;

    void beginConstraint(Constraint constraint) throws ReportException;

    void endConstraint() throws ReportException;

    void setResult(Result<? extends ExecutableRule> result) throws ReportException;
}
