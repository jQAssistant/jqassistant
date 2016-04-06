package com.buschmais.jqassistant.plugin.junit.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Failure")
public interface TestCaseFailureDescriptor extends JUnitDescriptor, TestCaseDetailDescriptor {
}
