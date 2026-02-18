package com.buschmais.jqassistant.core.report.api.model.source;

import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

import lombok.Builder.Default;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import static lombok.AccessLevel.PROTECTED;

/**
 * Abstract base class for source locations.
 *
 * @param <P>
 *     The parent type.
 * @deprecated To be replaced by {@link SourceLocation}.
 */
@Deprecated
@ToBeRemovedInVersion(major = 3, minor = 0)
@Getter
@SuperBuilder
@RequiredArgsConstructor(access = PROTECTED)
@ToString
public abstract class AbstractLocation<P extends AbstractLocation<?>> {

    /**
     * The optional parent containing the file.
     */
    @Default
    private final Optional<P> parent = Optional.empty();

    /**
     * The file name. If a {@link #parent} is available, then this file name is
     * relative to it.
     */
    private final String fileName;

}
