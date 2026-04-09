package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import static com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement.ClassFileDepdendency;

@Java(ClassFileDepdendency)
@Relation("DEPENDS_ON")
public interface ClassFileDependsOnDescriptor extends Descriptor {

    @Incoming
    ClassFileDescriptor getDependency();

    @Outgoing
    ClassFileDescriptor getDependent();

    Integer getWeight();

    void setWeight(Integer weight);

}
