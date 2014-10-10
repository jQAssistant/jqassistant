package com.buschmais.jqassistant.examples.plugins.scanner.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents a column within a row of a CSV file.
 */
@Label("Column")
public interface CSVColumnDescriptor extends CSVDescriptor {

    String getValue();

    void setValue(String value);

    int getIndex();

    void setIndex(int index);

}
