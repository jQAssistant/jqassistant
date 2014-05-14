package com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("TestCase")
public interface TestCaseDescriptor extends NamedDescriptor {

    public enum Result {
        SUCCESS, FAILURE, ERROR, SKIPPED;
    }

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
