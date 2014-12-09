package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("ErrorPage")
public interface ErrorPageDescriptor extends WebDescriptor {

    String getErrorPage();

    void setErrorPage(String errorPage);

    int getErrorCode();

    void setErrorCode(int errorCode);

    @Relation("FOR_EXCEPTION_TYPE")
    TypeDescriptor getExceptionType();

    void setExceptionType(TypeDescriptor typeDescriptor);

}
