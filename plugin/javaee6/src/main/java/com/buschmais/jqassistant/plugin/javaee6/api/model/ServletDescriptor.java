package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Servlet")
public interface ServletDescriptor extends WebDescriptor, NamedDescriptor, TypedDescriptor, AsyncSupportedDescriptor {

    @Relation("HAS_DESCRIPTION")
    List<DescriptionDescriptor> getDescriptions();

    @Relation("HAS_DISPLAY_NAME")
    List<DisplayNameDescriptor> getDisplayNames();

    boolean isEnabled();

    void setEnabled(boolean value);

    @Relation("HAS_ICON")
    List<IconDescriptor> getIcons();

    @Relation("HAS_INIT_PARAM")
    List<ParamValueDescriptor> getInitParams();

    String getJspFile();

    void setJspFile(String value);

    Boolean isLoadOnStartup();

    void setLoadOnStartup(Boolean loadOnStartup);

    @Relation("HAS_MULTI_PART_CONFIG")
    MultipartConfigDescriptor getMultipartConfig();

    void setMultipartConfig(MultipartConfigDescriptor multipartConfigDescriptor);

    @Relation("RUNNING_AS")
    RunAsDescriptor getRunAs();

    void setRunAs(RunAsDescriptor runAsDescriptor);

    @Relation("HAS_SECURITY_ROLE_REFS")
    List<SecurityRoleRefDescriptor> getSecurityRoleRefs();

}
