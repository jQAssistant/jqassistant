package com.buschmais.jqassistant.core.scanner.api.configuration;

import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.scan")
public interface Scan {

    String RESET = "reset";

    @Description("Indicates whether to initially reset the store (i.e. clear all nodes and relationships) before scanning.")
    Optional<Boolean> reset();

    String CONTINUE_ON_ERROR = "continue-on-error";

    @Description("Specifies if a scan shall be continued if an error is encountered.")
    @WithDefault("false")
    boolean continueOnError();

    String INCLUDE = "include";

    @Description("The items to include for scanning.")
    Optional<Include> include();

    String PROPERTIES = "properties";

    @Description("The properties to configure scanner plugins. The supported properties are plugin specific.")
    Map<String, String> properties();
}
