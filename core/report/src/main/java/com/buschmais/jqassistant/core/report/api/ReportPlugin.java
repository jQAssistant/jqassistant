package com.buschmais.jqassistant.core.report.api;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.shared.lifecycle.ContextualConfigurableLifecycleAware;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines the interface for report plugins.
 */
public interface ReportPlugin extends ContextualConfigurableLifecycleAware<ReportContext, Map<String, Object>> {

    /**
     * Marks a {@link ReportPlugin} as default, i.e. it will be executed for every
     * rule without explicit selection.
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    @interface Default {
    }

    /*
     * Initializes the plugin.
     */
    @Override
    default void initialize() throws ReportException {
    }

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
    @Override
    default void configure(ReportContext reportContext, Map<String, Object> properties) throws ReportException {
    }

    @Override
    default void destroy() throws ReportException {
    }

    default void begin() throws ReportException {
    }

    default void end() throws ReportException {
    }

    default void beginConcept(Concept concept) throws ReportException {
    }

    default void endConcept() throws ReportException {
    }

    default void beginGroup(Group group) throws ReportException {
    }

    default void endGroup() throws ReportException {
    }

    default void beginConstraint(Constraint constraint) throws ReportException {
    }

    default void endConstraint() throws ReportException {
    }

    default void setResult(Result<? extends ExecutableRule> result) throws ReportException {
    }
}
