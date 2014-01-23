package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;
import java.util.Collection;

import org.apache.maven.project.MavenProject;
import org.eclipse.tycho.core.TychoConstants;

import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.ProjectDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TychoProjectDescriptor;

/**
 * Implementation of the
 * {@link com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin} for
 * java classes.
 */
public class TychoProjectScannerPlugin implements ProjectScannerPlugin<ProjectDescriptor> {

	@Override
	public ProjectDescriptor scanProject(Store store, MavenProject project) {
		Object value = project.getContextValue(TychoConstants.CTX_ECLIPSE_PLUGIN_PROJECT);

		TychoProjectDescriptor descriptor = new TychoProjectDescriptor() {
		};
		return descriptor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin#
	 * getAdditionalFiles()
	 */
	@Override
	public Collection<? extends File> getAdditionalFiles() {
		// TODO Auto-generated method stub
		return null;
	}
}
