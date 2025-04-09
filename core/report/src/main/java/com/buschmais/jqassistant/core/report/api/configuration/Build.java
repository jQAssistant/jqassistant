package com.buschmais.jqassistant.core.report.api.configuration;

import java.time.ZonedDateTime;
import java.util.Map;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "jqassistant.analyze.report.build")
public interface Build {

    String NAME = "name";

    @Description("The name of the build.")
    String name();

    String TIMESTAMP = "timestamp";

    @Description("The build timestamp.")
    ZonedDateTime timestamp();

    String PROPERTIES = "properties";

    @Description("The build properties.")
    Map<String, String> properties();

}
