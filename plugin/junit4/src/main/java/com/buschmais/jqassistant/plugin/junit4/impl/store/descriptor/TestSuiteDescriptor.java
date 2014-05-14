package com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("TestSuite")
public interface TestSuiteDescriptor extends NamedDescriptor, FileDescriptor {

    @Property("tests")
    int getTests();

    void setTests(int tests);

    @Property("failures")
    int getFailures();

    void setFailures(int failures);

    @Property("erros")
    int getErrors();

    void setErrors(int errors);

    @Property("skipepd")
    int getSkipped();

    void setSkipped(int skipped);

    @Property("time")
    float getTime();

    void setTime(float time);

    @Relation("CONTAINS")
    List<TestCaseDescriptor> getTestCases();
}
