package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.catalog.schema.v1.JqassistantCatalog;

import javax.xml.transform.Source;
import java.util.List;

/**
 * Defines the interface for catalog readers.
 */
public interface CatalogReader {

    String CATALOG_RESOURCE = "META-INF/jqassistant-catalog.xml";
    String CATALOG_SCHEMA_RESOURCE = "/META-INF/xsd/jqassistant-catalog-1.0.xsd";

    List<Source> readCatalogs();
}
