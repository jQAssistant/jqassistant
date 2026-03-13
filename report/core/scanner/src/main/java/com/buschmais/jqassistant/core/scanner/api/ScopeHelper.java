package com.buschmais.jqassistant.core.scanner.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.slf4j.Logger;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Provides common functionality for working with scopes.
 */
public class ScopeHelper {

    public static final String SCOPE_SEPARATOR = "::";

    private final Logger logger;

    /**
     * Constructor.
     *
     * @param log
     *     The logger used to log all messages
     */
    public ScopeHelper(Logger log) {
        this.logger = log;
    }

    /**
     * Print a list of available scopes to the console.
     *
     * @param scopes
     *     The available scopes.
     */
    public void printScopes(Map<String, Scope> scopes) {
        logger.info("Scopes [" + scopes.size() + "]");
        for (String scopeName : scopes.keySet()) {
            logger.info("\t" + scopeName);
        }
    }

    public List<ScopedResource> getScopedResources(String resources) {
        return resources != null ? getScopedResources(asList(resources.split(","))) : Collections.emptyList();
    }

    public List<ScopedResource> getScopedResources(List<String> resources) {
        return resources.stream()
            .map(resource -> {
                String[] segments = resource.trim()
                    .split(SCOPE_SEPARATOR);
                if (segments.length == 2) {
                    return ScopedResource.builder()
                        .resource(segments[1])
                        .scopeName(segments[0])
                        .build();
                } else {
                    return ScopedResource.builder()
                        .resource(segments[0])
                        .build();
                }
            })
            .collect(toList());
    }

    /**
     * Represents a scanable resource (e.g. file, URL) that can optionally be scoped
     * (e.g. java:classpath, maven:repository).
     */
    @Builder
    @Getter
    @EqualsAndHashCode
    @ToString
    public static class ScopedResource {

        private String resource;

        private String scopeName;
    }
}
