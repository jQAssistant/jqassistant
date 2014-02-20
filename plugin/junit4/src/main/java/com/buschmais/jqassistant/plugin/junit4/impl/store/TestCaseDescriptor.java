package com.buschmais.jqassistant.plugin.junit4.impl.store;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ClassTypeDescriptor;

@Label("TESTCASE")
public interface TestCaseDescriptor extends NamedDescriptor {

    public enum Result {
        SUCCESS,
        FAILURE,
        ERROR,
        SKIPPED;
    }


    @Relation("DECLARED_IN")
    ClassTypeDescriptor getDeclaredIn();

    void setDeclaredIn(ClassTypeDescriptor declaredIn);

    @Property("TIME")
    float getTime();

    void setTime(float time);

    @Property("RESULT")
    Result getResult();

    void setResult(Result result);

}
