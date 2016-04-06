package com.buschmais.jqassistant.plugin.junit.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("TestCase")
public interface TestCaseDescriptor extends JUnitDescriptor, NamedDescriptor {

    enum Result {
        SUCCESS, FAILURE, ERROR, SKIPPED
    }

    @Relation("HAS_ERROR")
    TestCaseErrorDescriptor getError();

    void setError(TestCaseErrorDescriptor errorDescriptor);

    @Relation("HAS_FAILURE")
    TestCaseFailureDescriptor getFailure();

    void setFailure(TestCaseFailureDescriptor failureDescriptor);

    @Property("className")
    String getClassName();

    void setClassName(String className);

    @Property("time")
    float getTime();

    void setTime(float time);

    @Property("result")
    Result getResult();

    void setResult(Result result);

}
