package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.ApplicationDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlFileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface ApplicationXmlDescriptor extends EnterpriseDescriptor, ApplicationDescriptor, XmlFileDescriptor,
                                                  NamedDescriptor, VersionDescriptor {

    @Relation("HAS_DISPLAY_NAME")
    List<DisplayNameDescriptor> getDisplayNames();

    @Relation("HAS_DESCRIPTION")
    List<DescriptionDescriptor> getDescriptions();

    @Relation("HAS_ICON")
    List<IconDescriptor> getIcons();

    @Relation("HAS_MODULE")
    List<EnterpriseApplicationModuleDescriptor> getModules();

    @Relation("HAS_SECURITY_ROLE")
    List<SecurityRoleDescriptor> getSecurityRoles();

    String getInitializeInOrder();

    void setInitializeInOrder(String initializeInOrder);

    String getLibraryDirectory();

    void setLibraryDirectory(String libraryDirectory);

}
