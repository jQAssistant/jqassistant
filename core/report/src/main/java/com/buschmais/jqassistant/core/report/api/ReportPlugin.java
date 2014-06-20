package com.buschmais.jqassistant.core.report.api;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException;

public interface ReportPlugin extends AnalysisListener {

    void initialize(Map<String, Object> properties) throws AnalysisListenerException;
}
