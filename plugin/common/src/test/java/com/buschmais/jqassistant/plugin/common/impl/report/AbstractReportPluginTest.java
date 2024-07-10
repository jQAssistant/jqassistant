package com.buschmais.jqassistant.plugin.common.impl.report;

import java.io.File;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.store.api.Store;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

/**
 * Abstract base class for {@link ReportPlugin} unit tests.
 */
public abstract class AbstractReportPluginTest {

    protected final ReportPlugin plugin;

    @Mock
    protected Store store;

    protected ReportContext reportContext;

    protected AbstractReportPluginTest(ReportPlugin plugin) {
        this.plugin = plugin;
    }

    @BeforeEach
    public final void setUp() throws ReportException {
        plugin.initialize();
        File outputDirectory = new File("target/test");
        reportContext = new ReportContextImpl(AbstractReportPluginTest.class.getClassLoader(), store, outputDirectory);
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
