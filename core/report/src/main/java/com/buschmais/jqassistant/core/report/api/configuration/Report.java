package com.buschmais.jqassistant.core.report.api.configuration;

import java.util.Map;

import com.buschmais.jqassistant.core.shared.annotation.Description;

public interface Report {

    String PREFIX = "jqassistant.analyze.report";

    String PROPERTIES = "properties";

    @Description("The properties to configure report plugins. The supported properties are plugin specific.")
    Map<String, String> properties();
}
