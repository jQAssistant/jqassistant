package com.buschmais.jqassistant.core.scanner.api;

import java.io.File;
import java.util.List;

import org.apache.maven.project.MavenProject;


/**
 * Defines the interface for a project scanner.
 */
public interface ProjectScanner {

	/**
	 * @param project
	 * @return
	 */
	List<File> getAdditionalFiles(MavenProject project);
}
