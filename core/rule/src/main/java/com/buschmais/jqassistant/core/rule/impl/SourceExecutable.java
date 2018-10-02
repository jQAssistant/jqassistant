package com.buschmais.jqassistant.core.rule.impl;

import com.buschmais.jqassistant.core.analysis.api.rule.Executable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * The implementation of
 * {@link com.buschmais.jqassistant.core.analysis.api.rule.Executable}.
 */
@Getter
@ToString
@AllArgsConstructor
public class SourceExecutable<S> implements Executable<S>  {

    private String language;

    private S source;

    private Class<S> type;

}
