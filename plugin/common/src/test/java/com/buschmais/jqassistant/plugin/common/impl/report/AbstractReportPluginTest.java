package com.buschmais.jqassistant.plugin.common.impl.report;

import java.io.File;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;

import org.junit.Before;

/**
 * Abstract base class for {@link ReportPlugin} unit tests.
 */
public abstract class AbstractReportPluginTest {

    protected final ReportPlugin plugin;

    protected ReportContext reportContext;

    protected AbstractReportPluginTest(ReportPlugin plugin) {
        this.plugin = plugin;
    }

    @Before
    public final void setUp() throws ReportException {
        plugin.initialize();
        File outputDirectory = new File("target/test");
        reportContext = new ReportContextImpl(outputDirectory);
    }

    protected final void apply(Constraint constraint, Result.Status status) throws ReportException {
        plugin.beginConstraint(constraint);
        plugin.setResult(this.<ExecutableRule> getResult(constraint, status));
        plugin.endConstraint();
    }

    protected final void apply(Concept concept, Result.Status status) throws ReportException {
        plugin.beginConcept(concept);
        plugin.setResult(this.<ExecutableRule> getResult(concept, status));
        plugin.endConcept();
    }

    /**
     * Return the {@link Result} for the given rule.
     *
     * @param rule
     *            The rule.
     * @param status
     *            The status.
     * @param <T>
     *            The rule type.
     * @return The result.
     */
    protected abstract <T extends ExecutableRule<?>> Result<T> getResult(T rule, Result.Status status);

}
