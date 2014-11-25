package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ApplicationDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.xo.api.annotation.Transient;

/**
 * Describes a WAR archive.
 */
public interface WebApplicationArchiveDescriptor extends WebDescriptor, ApplicationDescriptor, ArchiveDescriptor {

    @Transient
    JavaClassesDirectoryDescriptor getClassesDirectory();

    void setClassesDirectory(JavaClassesDirectoryDescriptor classesDirectory);

}
