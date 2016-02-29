package com.buschmais.jqassistant.sonar.plugin.sensor;

import org.sonar.api.BatchExtension;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

/**
 * Defines an interface for resolving resources representing language specific
 * elements, e.g. java classes.
 */
public interface LanguageResourceResolver extends BatchExtension {

	/**
	 * Return the language this resolver represents.
	 * 
	 * @return The language.
	 */
	String getLanguage();

	/**
	 * Resolve the resource for an element of a given type.
	 * @param nodeType
	 *            The type declaration in report.
	 * @param nodeSource 
	 * 			The source name producing the node element in report (e.g. the class file name for java classes).
	 * @param nodeValue
	 *            The value of the node element in report (e.g. the class name).
	 * 
	 * @return The resource or <code>null</code> if not resolved.
	 */
	Resource resolve(Project project, String nodeType, String nodeSource, String nodeValue);
}
