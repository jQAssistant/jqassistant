package com.buschmais.jqassistant.core.scanner.api.configuration;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.WithDefault;

@Description("The configuration to be used for scanning.")
public interface Scan {

    @Description("Indicates whether to initially reset the store (i.e. clear all nodes and relationships) before scanning.")
    @WithDefault("false")
    boolean reset();

    @Description("Specifies if a scan shall be continued if an error is encountered.")
    @WithDefault("false")
    boolean continueOnError();

    @Description("The items and (e.g. files, URLs) to include for scanning. Every item may be prefixed by a scope using '::' as separator., e.g. 'java:classpath::build/classes'.")
    List<String> include();

    @Description("The properties to configure scanner plugins. The supported properties are plugin specific.")
    Map<String, String> properties();
}
