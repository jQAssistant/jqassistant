package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Defines a descriptor having a signature.
 */
public interface SignatureDescriptor {

    @Property("SIGNATURE")
	String getSignature();

	void setSignature(String signature);
}
