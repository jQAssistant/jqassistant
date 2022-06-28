package com.buschmais.jqassistant.core.report.api.configuration;

import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.analyze.report")
public interface Report {

    String PROPERTIES = "properties";

    @Description("The properties to configure report plugins. The supported properties are plugin specific.")
    Map<String, String> properties();

    String WARN_ON_SEVERITY = "warn-on-severity";

    @Description("Determines the severity level to report warnings for rules with equal or higher severities.")
    @WithDefault("MINOR")
    @WithConverter(SeverityThresholdConverter.class)
    Severity.Threshold warnOnSeverity();

    String FAIL_ON_SEVERITY = "fail-on-severity";

    @Description("Determines the severity level to report failures for rules with equal or higher severities.")
    @WithDefault("MAJOR")
    @WithConverter(SeverityThresholdConverter.class)
    Severity.Threshold failOnSeverity();

    String CREATE_ARCHIVE = "create-archive";

    @Description("Create an archive containing all generated reports.")
    @WithDefault("false")
    boolean createArchive();
}
