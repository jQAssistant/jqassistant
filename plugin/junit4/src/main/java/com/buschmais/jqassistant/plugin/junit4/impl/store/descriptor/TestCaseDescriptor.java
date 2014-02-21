package com.buschmais.jqassistant.plugin.junit4.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.NamedDescriptor;

@Label("TESTCASE")
public interface TestCaseDescriptor extends NamedDescriptor {

    public enum Result {
        SUCCESS,
        FAILURE,
        ERROR,
        SKIPPED;
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
