package com.buschmais.jqassistant.core.scanner.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.project.MavenProject;

/**
 * Defines the interface for a project scanner.
 */
public interface ProjectScanner {

	/**
	 * Provides all files, which the ProjectScanner found for processing with a
	 * FileScannerPlugin.
	 * 
	 * @param project
	 *            A common {@link MavenProject}
	 * @throws IOException
	 *             If scanning fails.
	 */
	List<File> getAdditionalFiles(MavenProject project) throws IOException;
}
