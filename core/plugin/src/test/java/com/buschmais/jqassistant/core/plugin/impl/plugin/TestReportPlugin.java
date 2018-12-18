package com.buschmais.jqassistant.core.plugin.impl.plugin;

import java.util.Map;

import com.buschmais.jqassistant.core.report.api.AbstractReportPlugin;
import com.buschmais.jqassistant.core.report.api.ReportContext;

public class TestReportPlugin extends AbstractReportPlugin {

    private Map<String, Object> properties;

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }
}
