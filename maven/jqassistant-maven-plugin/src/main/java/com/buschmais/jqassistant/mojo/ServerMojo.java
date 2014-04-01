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

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.core.store.impl.Server;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.Set;

/**
 * Starts an embedded Neo4j server.
 */
@Mojo(name = "server")
public class ServerMojo extends AbstractAnalysisAggregatorMojo {

    @Override
    protected void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException,
            MojoFailureException {
        Server server = new Server((EmbeddedGraphStore) store);
        server.start();
        getLog().info("Running server for module " + baseProject.getGroupId() + ":" + baseProject.getArtifactId() + ":" + baseProject.getVersion());
        getLog().info("Press <Enter> to finish.");
        try {
            System.in.read();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot read from System.in.", e);
        } finally {
            server.stop();
        }
    }

    @Override
    protected boolean isResetStoreOnInitialization() {
        return false;
    }

}
