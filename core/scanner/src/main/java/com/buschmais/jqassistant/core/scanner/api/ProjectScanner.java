package com.buschmais.jqassistant.core.scanner.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;


/**
 * Defines the interface for a project scanner.
 */
public interface ProjectScanner {

	/**
	 * @param project
	 * @return
	 * @throws MojoExecutionException
	 */
	List<File> getAdditionalFiles(MavenProject project) throws IOException;
}
