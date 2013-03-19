package com.buschmais.jqassistant.store;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.model.ClassDescriptor;

public interface GraphStore {

    void start();

    void stop();

    void createClassNodesWithDependencies(Map<ClassDescriptor, Set<ClassDescriptor>> dependencies);

}