package com.buschmais.jqassistant.examples.plugins.scanner.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

// tag::class[]
/**
 * Represents a row of a CSV file.
 */
@Label("Row")
public interface CSVRowDescriptor extends CSVDescriptor {

    int getLineNumber();

    void setLineNumber(int lineNumber);

    @Relation("HAS_COLUMN")
    List<CSVColumnDescriptor> getColumns();

}
// end::class[]