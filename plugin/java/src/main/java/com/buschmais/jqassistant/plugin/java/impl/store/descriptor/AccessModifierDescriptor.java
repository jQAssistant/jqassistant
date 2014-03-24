package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Interface that describes java elements with access modifiers.
 *
 * @author Herklotz
 */
public interface AccessModifierDescriptor {

    @Property("VISIBILITY")
	VisibilityModifier getVisibility();

	void setVisibility(VisibilityModifier visibilityModifier);

    @Property("STATIC")
	Boolean isStatic();

	void setStatic(Boolean s);

    @Property("FINAL")
	Boolean isFinal();

	void setFinal(Boolean f);

    @Property("SYNTHETIC")
	public Boolean isSynthetic();

	public void setSynthetic(Boolean s);
}
