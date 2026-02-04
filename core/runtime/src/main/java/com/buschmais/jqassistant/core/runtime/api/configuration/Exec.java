package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Configuration for the exec command.
 */
@ConfigMapping(prefix = "jqassistant.exec")
public interface Exec {

    String QUERY = "query";

    @Description("The Cypher query to execute against the store.")
    Optional<String> query();

    String QUERY_FILE = "query-file";

    @Description("Path to a file containing the Cypher query to execute.")
    Optional<String> queryFile();

    String READ_ONLY = "read-only";

    @Description("Execute the query in read-only mode (changes will be rolled back).")
    @WithDefault("false")
    boolean readOnly();

    String OUTPUT_FORMAT = "output-format";

    @Description("Output format for query results: 'table' (default), 'json', or 'csv'.")
    @WithDefault("table")
    String outputFormat();

    String OUTPUT_FILE = "output-file";

    @Description("Optional file path to write query results to. If not specified, results are written to stdout.")
    Optional<String> outputFile();
}
