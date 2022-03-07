package com.buschmais.jqassistant.core.scanner.api.configuration;

import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

@Description("The items and (e.g. files, URLs) to include for scanning. Every item may be prefixed by a scope using '::' as separator., e.g. 'java:classpath::build/classes'.")
public interface Include {

    String PREFIX = "jqassistant.scan.include";

    String FILES = "files";

    @Description("The files to include.")
    Optional<List<String>> files();

    String URLS = "urls";

    @Description("The URLs to include.")
    Optional<List<String>> urls();

}
