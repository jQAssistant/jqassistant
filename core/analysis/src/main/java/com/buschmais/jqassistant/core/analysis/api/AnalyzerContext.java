package com.buschmais.jqassistant.core.analysis.api;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.Verification;
import com.buschmais.jqassistant.core.analysis.impl.VerificationStrategy;
import com.buschmais.jqassistant.core.store.api.Store;

import org.slf4j.Logger;

public interface AnalyzerContext {

    Store getStore();

    Logger getLogger();

    Map<Class<? extends Verification>, VerificationStrategy> getVerificationStrategies();

}
