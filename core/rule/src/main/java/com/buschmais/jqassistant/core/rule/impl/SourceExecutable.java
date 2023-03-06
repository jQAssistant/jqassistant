package com.buschmais.jqassistant.core.rule.impl;

import com.buschmais.jqassistant.core.rule.api.model.Executable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * The implementation of
 * {@link Executable}.
 */
@Getter
@ToString
@AllArgsConstructor
public class SourceExecutable<S> implements Executable<S>  {

    private String language;

    private S source;

    private Class<S> type;

    private boolean transactional;

}
