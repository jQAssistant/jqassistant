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

import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.store.impl.Server;

/**
 * @goal server
 * @requiresProject false
 */
public class ServerMojo extends AbstractJQAssistantMojo {

	@Override
	protected void execute(Store store) throws MojoExecutionException {
		Server server = new Server((EmbeddedGraphStore) store);
		server.start();
		try {
			getLog().info("Press <Enter> to finish.");
			System.in.read();
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Error while reading from System.in.", e);
		} finally {
			server.stop();
		}
	}
}