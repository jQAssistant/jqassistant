package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.model.Verification;
import com.buschmais.jqassistant.core.rule.api.reader.RowCountVerification;
import com.buschmais.jqassistant.core.store.api.Store;

import org.slf4j.Logger;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.of;

/**
 * Implementation of the {@link AnalyzerContext}.
 */
class AnalyzerContextImpl implements AnalyzerContext {

    private static final Verification DEFAULT_VERIFICATION = RowCountVerification.builder()
        .build();

    private final ClassLoader classLoader;

    private final Store store;

    private final Logger logger;

    private final Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies;

    AnalyzerContextImpl(Analyze configuration, ClassLoader classLoader, Store store, Logger logger) {
        this.classLoader = classLoader;
        this.store = store;
        this.logger = logger;
        this.verificationStrategies = of(new RowCountVerificationStrategy(configuration.report()),
            new AggregationVerificationStrategy(configuration.report())).collect(toMap(strategy -> strategy.getVerificationType(), strategy -> strategy));
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
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
    public <T extends ExecutableRule<?>> Result.Status verify(T executable, Severity severity, List<String> columnNames, List<Map<String, Object>> rows)
        throws RuleException {
        Verification verification = executable.getVerification();
        if (verification == null) {
            getLogger().debug("Using default verification for '{}'.", executable);
            verification = DEFAULT_VERIFICATION;
        }
        VerificationStrategy strategy = verificationStrategies.get(verification.getClass());
        if (strategy == null) {
            throw new RuleException("Result verification not supported: " + verification.getClass()
                .getName());
        }
        return strategy.verify(executable, severity, verification, columnNames, rows);
    }
}
