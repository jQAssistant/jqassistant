package com.buschmais.jqassistant.plugin.javaee6.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("SecurityConstraint")
public interface SecurityConstraintDescriptor extends WebDescriptor {

    @Relation("HAS_DISPLAY_NAME")
    List<DisplayNameDescriptor> getDisplayNames();

    @Relation("HAS_USER_DATA_CONSTRAINT")
    List<UserDataConstraintDescriptor> getUserDataConstraints();

    @Relation("HAS_AUTH_CONSTRAINT")
    List<AuthConstraintDescriptor> getAuthConstraints();

    @Relation("HAS_WEB_RESOURCE_COLLECTION")
    List<WebResourceCollectionDescriptor> getWebResourceCollections();
}
