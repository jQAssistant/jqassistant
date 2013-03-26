/**
 * Copyright (C) 2011 tdarby <tim.darby.uk@googlemail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.buschmais.jqassistant.mojo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;

import com.buschmais.jqassistant.scanner.DependencyScanner;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;

/**
 * @phase verify
 * @goal analyze
 * @requiresDependencyResolution test
 */
public class JQAssistentMojo extends AbstractMojo {
	/**
	 * The Maven Project Object
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 */
	protected MavenProject project;

	/**
	 * Used to build a maven projects.
	 * 
	 * @parameter 
	 *            expression="${component.org.apache.maven.project.MavenProjectBuilder}"
	 * @readonly
	 */
	protected MavenProjectBuilder projectBuilder;

	/**
	 * Location of the local repository.
	 * 
	 * @parameter expression="${localRepository}"
	 * @readonly
	 */
	protected ArtifactRepository localRepository;
	/**
	 * List of Remote Repositories used by the resolver
	 * 
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @readonly
	 */
	protected List<ArtifactRepository> remoteRepositories;

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

	@Override
	public void execute() throws MojoExecutionException {
		File databaseDirectory = new File(buildDirectory, "jqassistent");
		Store store = new EmbeddedGraphStore(
				databaseDirectory.getAbsolutePath());
		DependencyScanner scanner = new DependencyScanner(store);
		try {
			scanner.scanDirectory(classesDirectory);
		} catch (IOException e) {
			throw new MojoExecutionException("Cannot scan classes.", e);
		}

	}
}