/**
 *
 */
package com.buschmais.jqassistant.core.model.api.descriptor;

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

}
