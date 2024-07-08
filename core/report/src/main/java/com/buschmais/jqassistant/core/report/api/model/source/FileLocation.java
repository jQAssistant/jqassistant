package com.buschmais.jqassistant.core.report.api.model.source;

import java.util.Optional;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import static java.util.Optional.empty;

/**
 * Represents a source file location including an optional line number.
 */
@Getter
@SuperBuilder
@ToString
public class FileLocation extends AbstractLocation<ArtifactLocation> {

    @Default
    private final Optional<Integer> startLine = empty();

    @Default
    private final Optional<Integer> endLine = empty();

}
