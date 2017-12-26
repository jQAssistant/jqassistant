package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.rule.Verification;
import com.buschmais.jqassistant.core.store.api.Store;

import org.slf4j.Logger;

public class AnalyzerContextImpl implements AnalyzerContext {

    private Store store;

    private Logger logger;

    private Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies;

    public AnalyzerContextImpl(Store store, Logger logger, Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies) {
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
    public Map<Class<? extends Verification>, VerificationStrategy> getVerificationStrategies() {
        return verificationStrategies;
    }
}
