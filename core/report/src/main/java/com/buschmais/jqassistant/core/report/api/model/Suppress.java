package com.buschmais.jqassistant.core.report.api.model;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Abstract
@Label("jQASuppress")
public interface Suppress {

    String[] getSuppressIds();

    void setSuppressIds(String[] suppressIds);

    String getSuppressColumn();
    void setSuppressColumn(String suppressColumn);

}
