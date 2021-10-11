package com.buschmais.jqassistant.core.report.api.model.source;

import java.util.Optional;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents an artifact location with optional coordinates.
 */
@Getter
@SuperBuilder
@ToString
public class ArtifactLocation extends AbstractLocation<ArtifactLocation> {

    private Optional<String> group;

    private Optional<String> name;

    private Optional<String> version;

    private Optional<String> classifier;

    private Optional<String> type;

}
