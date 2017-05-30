package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static lombok.AccessLevel.PRIVATE;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
@ToString
public class ClassModelConfiguration {

    @Default
    private boolean typeDependsOnWeight = true;

    @Default
    private boolean methodDeclaresVariable = true;

}
