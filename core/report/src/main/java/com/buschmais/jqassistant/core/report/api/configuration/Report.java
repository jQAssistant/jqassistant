package com.buschmais.jqassistant.core.report.api.configuration;

import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.WithDefault;

public interface Report {

    String PREFIX = "jqassistant.analyze.report";

    String PROPERTIES = "properties";

    @Description("The properties to configure report plugins. The supported properties are plugin specific.")
    Map<String, String> properties();

    String WARN_ON_SEVERITY = "warn-on-severity";

    @Description("Determines the severity level for issuing warnings for failed with equal or higher severities.")
    @WithDefault("MINOR")
    Severity warnOnSeverity();

    String FAIL_ON_SEVERITY = "fail-on-severity";

    @Description("Determines the severity level for breaking the build if at least one rule with an equal or higher severity failed.")
    @WithDefault("MAJOR")
    Severity failOnSeverity();

    String CREATE_ARCHIVE = "create-archive";

    @Description("Create an archive containing all generated reports.")
    @WithDefault("false")
    boolean createArchive();
}
