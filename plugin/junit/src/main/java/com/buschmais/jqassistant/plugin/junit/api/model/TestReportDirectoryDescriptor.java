package com.buschmais.jqassistant.plugin.junit.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("TestReport")
public interface TestReportDirectoryDescriptor extends JUnitDescriptor, DirectoryDescriptor {
}
