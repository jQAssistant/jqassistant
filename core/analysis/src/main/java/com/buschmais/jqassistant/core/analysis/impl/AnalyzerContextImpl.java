package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.model.Verification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.store.api.Store;

import org.slf4j.Logger;

/**
 * Implementation of the {@link AnalyzerContext}.
 */
public class AnalyzerContextImpl implements AnalyzerContext {

    private static final Verification DEFAULT_VERIFICATION = RowCountVerification.builder().build();

    private Store store;

    private Logger logger;

    private Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies;

    /**
     * Constructor.
     *
     * @param store
     *            The {@link Store}.
     * @param logger
     *            The {@link Logger}.
     * @param verificationStrategies
     *            The {@link VerificationStrategy}s.
     */
    AnalyzerContextImpl(Store store, Logger logger, Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies) {
        this.store = store;
        this.logger = logger;
        this.verificationStrategies = verificationStrategies;
    }

    @Override
    public Store getStore() {
        return store;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public <T extends ExecutableRule<?>> Result.Status verify(T executable, List<String> columnNames, List<Map<String, Object>> rows)
            throws RuleException {
        Verification verification = executable.getVerification();
        if (verification == null) {
            getLogger().debug("Using default verification for '{}'." + executable);
            verification = DEFAULT_VERIFICATION;
        }
        return verify(executable, columnNames, rows, verification);
    }

    @Override
    public <T extends ExecutableRule<?>> Result.Status verify(T executable, List<String> columnNames, List<Map<String, Object>> rows, Verification verification) throws RuleException {
        VerificationStrategy strategy = verificationStrategies.get(verification.getClass());
        if (strategy == null) {
            throw new RuleException("Result verification not supported: " + verification.getClass().getName());
        }
        return strategy.verify(executable, verification, columnNames, rows);
    }

    @Override
    public <R extends ExecutableRule<?>> Result.ResultBuilder<R> resultBuilder(R rule, Severity severity) {
        return Result.<R>builder().rule(rule).severity(severity);
    };
}
