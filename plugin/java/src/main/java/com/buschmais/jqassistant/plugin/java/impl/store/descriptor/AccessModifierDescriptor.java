package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

/**
 * Interface that describes java elements with access modifiers.
 * 
 * @author Herklotz
 */
public interface AccessModifierDescriptor {

	VisibilityModifier getVisibility();

	void setVisibility(VisibilityModifier visibilityModifier);

	Boolean isStatic();

	void setStatic(Boolean s);

	Boolean isFinal();

	void setFinal(Boolean f);

	public Boolean isSynthetic();

	public void setSynthetic(Boolean s);
}
