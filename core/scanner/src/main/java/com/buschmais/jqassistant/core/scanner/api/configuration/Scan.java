package com.buschmais.jqassistant.core.scanner.api.configuration;

import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.WithDefault;

public interface Scan {

    String PREFIX = "jqassistant.scan";

    String RESET = "reset";

    @Description("Indicates whether to initially reset the store (i.e. clear all nodes and relationships) before scanning.")
    @WithDefault("false")
    boolean reset();

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
