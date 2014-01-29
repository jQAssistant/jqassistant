package com.buschmais.jqassistant.core.scanner.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.scanner.api.ProjectScanner;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.ProjectDescriptor;

/**
 * Implementation of the {@link ProjectScanner}.
 */
public class ProjectScannerImpl implements ProjectScanner {

	private final List<ProjectScannerPlugin<?>> projectScannerPlugins;
	private final Store store;

	public ProjectScannerImpl(Store store, List<ProjectScannerPlugin<?>> projectScannerPlugins) {
		this.store = store;
		this.projectScannerPlugins = projectScannerPlugins;
	}

	@Override
	public List<File> getAdditionalFiles(MavenProject project) throws IOException {
		List<File> files = new ArrayList<>();
		for (ProjectScannerPlugin<?> plugin : projectScannerPlugins) {
			ProjectDescriptor scannedProject = plugin.scanProject(store, project);
			files.addAll(scannedProject.getAdditionalFiles());
		}
		return files;
	}

}
