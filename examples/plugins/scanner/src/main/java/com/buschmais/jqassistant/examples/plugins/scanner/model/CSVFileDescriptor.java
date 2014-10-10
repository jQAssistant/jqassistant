package com.buschmais.jqassistant.examples.plugins.scanner.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents a CSV file.
 * <p>
 * The labels are inherited from {@link CSVDescriptor} and
 * {@link FileDescriptor}.
 * </p>
 */
public interface CSVFileDescriptor extends CSVDescriptor, FileDescriptor {

    @Relation("HAS_ROW")
    List<CSVRowDescriptor> getRows();

}
