package com.buschmais.jqassistant.core.report.api.model.source;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Abstract base class for source locations.
 *
 * @param <P>
 *     The parent type.
 */
@Getter
@SuperBuilder
@ToString
public abstract class SourceLocation<P extends SourceLocation<?>> extends AbstractLocation<P> {
}
