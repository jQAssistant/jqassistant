package com.buschmais.jqassistant.mojo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.buschmais.jqassistant.store.api.Store;

public abstract class AbstractJQAssistantMojo extends AbstractMojo {

	/**
	 * The artifactId.
	 * 
	 * @parameter expression="${project.artifactId}"
	 * @readonly
	 */
	protected String artifactId;

	/**
	 * The build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @readonly
	 */
	protected File buildDirectory;

	/**
	 * The classes directory.
	 * 
	 * @parameter expression="${project.build.outputDirectory}"
	 * @readonly
	 */
	protected File classesDirectory;

	/**
	 * The classes directory.
	 * 
	 * @parameter expression="${project.build.testOutputDirectory}"
	 * @readonly
	 */
	protected File testClassesDirectory;

	/**
	 * The build directory.
	 * 
	 * @parameter expression="${jqassistant.store.directory}"
	 * @readonly
	 */
	protected File storeDirectory;

	/**
	 * @component
	 */
	protected StoreProvider storeProvider;

	@Override
	public final void execute() throws MojoExecutionException {
		File databaseDirectory;
		if (storeDirectory != null) {
			databaseDirectory = storeDirectory;
		} else {
			databaseDirectory = new File(buildDirectory, "jqassistant");
		}
		Store store = storeProvider.getStore(databaseDirectory);
		execute(store);
	}

	protected abstract void execute(Store store) throws MojoExecutionException;
}
