package com.buschmais.jqassistant.plugin.junit.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("TestSuite")
public interface TestSuiteDescriptor extends JUnitDescriptor, NamedDescriptor, XmlFileDescriptor {

    @Property("tests")
    int getTests();

    void setTests(int tests);

    @Property("failures")
    int getFailures();

    void setFailures(int failures);

    @Property("errors")
    int getErrors();

    void setErrors(int errors);

    @Property("skipped")
    int getSkipped();

    void setSkipped(int skipped);

    @Property("time")
    float getTime();

    void setTime(float time);

    @Relation("CONTAINS")
    List<TestCaseDescriptor> getTestCases();
}
