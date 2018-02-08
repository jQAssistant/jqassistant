package com.buschmais.jqassistant.core.analysis.api.rule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import static lombok.AccessLevel.PROTECTED;

@Getter
@ToString
@AllArgsConstructor(access = PROTECTED)
public abstract class AbstractExecutable implements Executable {

    private final String language;

    private final String source;

}
