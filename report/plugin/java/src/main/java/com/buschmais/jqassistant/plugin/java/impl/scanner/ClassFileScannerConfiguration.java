package com.buschmais.jqassistant.plugin.java.impl.scanner;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class ClassFileScannerConfiguration {

    private boolean includeLocalVariables;

}
