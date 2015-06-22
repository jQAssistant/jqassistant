package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.Set;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileNameDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Representing a JSF template file in the jqassistant store.
 *
 * @author peter.herklotz@buschmais.com
 */
@Label(value = "Facelet", usingIndexedPropertyOf = FileNameDescriptor.class)
public interface JsfFaceletDescriptor extends JsfDescriptor, FileDescriptor {

    /**
     * Included JSF Templates.
     *
     * @return a Set of {@link JsfFaceletDescriptor} that are included in the
     *         current template.
     */
    @Relation("INCLUDES")
    Set<JsfFaceletDescriptor> getIncludes();

    /**
     * Sets a set of included files.
     *
     * @param includes
     *            the descriptors of the files.
     */
    void setIncludes(Set<JsfFaceletDescriptor> includes);

    /**
     * If the current {@link JsfFaceletDescriptor} links to a template it can
     * requested with this method.
     *
     * @return the {@link JsfFaceletDescriptor} of the Template or
     *         <code>null</code>
     */
    @Relation("WITH_TEMPLATE")
    JsfFaceletDescriptor getTemplate();

    /**
     * Sets a Template Descriptor.
     *
     * @param template
     *            the {@link JsfFaceletDescriptor}
     */
    void setTemplate(JsfFaceletDescriptor template);

}
