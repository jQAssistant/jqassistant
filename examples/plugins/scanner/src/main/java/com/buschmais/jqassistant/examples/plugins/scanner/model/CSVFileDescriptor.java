package com.buschmais.jqassistant.examples.plugins.scanner.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

// tag::class[]
/**
 * Represents a CSV file. The labels are inherited from {@link CSVDescriptor}
 * and {@link FileDescriptor}.
 */
public interface CSVFileDescriptor extends CSVDescriptor, FileDescriptor {

    @Relation("HAS_ROW")
    List<CSVRowDescriptor> getRows();

}
// end::class[]