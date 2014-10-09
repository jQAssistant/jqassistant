package com.buschmais.jqassistant.examples.plugins.scanner.model;

import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Created by dimahler on 10/9/2014.
 */
@Label("CSV")
public interface CSVFileModel extends FileDescriptor {
}
