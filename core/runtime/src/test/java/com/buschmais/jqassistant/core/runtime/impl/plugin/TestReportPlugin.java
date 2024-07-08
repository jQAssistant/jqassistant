package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;

public class TestReportPlugin implements ReportPlugin {

    private Map<String, Object> properties;

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
