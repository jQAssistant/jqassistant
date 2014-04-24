package com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("TESTCASE")
public interface TestCaseDescriptor extends NamedDescriptor {

    public enum Result {
        SUCCESS, FAILURE, ERROR, SKIPPED;
    }

    @Property("CLASSNAME")
    String getClassName();

    void setClassName(String className);

    @Property("TIME")
    float getTime();

    void setTime(float time);

    @Property("RESULT")
    Result getResult();

    void setResult(Result result);

}
