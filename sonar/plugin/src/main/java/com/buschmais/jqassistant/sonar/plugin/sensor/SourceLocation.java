package com.buschmais.jqassistant.sonar.plugin.sensor;

import org.sonar.api.resources.Resource;

/**
 * Helper class to hold resource + line number of source together.
 *
 * @author rzozmann
 *
 */
final class SourceLocation {
	final Resource resource;
	final Integer lineNumber;

	SourceLocation(Resource resource, Integer lineNumber) {
		this.resource = resource;
		this.lineNumber = lineNumber;
	}
}