package com.buschmais.jqassistant.plugin.facelet.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Descriptor of a JSF artifact.
 *
 * @author peter.herklotz@buschmais.com
 */
@Label("Jsf")
public interface JsfDescriptor extends Descriptor {
}
