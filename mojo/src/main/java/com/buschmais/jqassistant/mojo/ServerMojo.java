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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.AbstractGraphStore;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.store.impl.Server;

/**
 * @phase verify
 * @goal server
 * @requiresDependencyResolution test
 * @aggregator true
 */
public class ServerMojo extends AbstractMojo {

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
	 * The build directory.
	 * 
	 * @parameter expression="${jqassistant.store.directory}"
	 * @readonly
	 */
	protected File storeDirectory;

	@Override
	public void execute() throws MojoExecutionException {
		File databaseDirectory;
		if (storeDirectory != null) {
			databaseDirectory = storeDirectory;
		} else {
			databaseDirectory = new File(buildDirectory, "jqassistent");
		}
		Store store = new EmbeddedGraphStore(
				databaseDirectory.getAbsolutePath());
		store.start();
		try {
			Server server = new Server((AbstractGraphStore) store);
			server.start();
			try {
				System.out.println("Waiting");
			} finally {
				server.stop();
			}
		} finally {
			store.stop();
		}

	}
}