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

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.scanner.impl.ClassScannerImpl;
import com.buschmais.jqassistant.store.api.Store;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

/**
 * @phase package
 * @goal scan
 * @requiresDependencyResolution test
 */
public class ScanMojo extends AbstractStoreMojo {

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        scanDirectory(classesDirectory);
        scanDirectory(testClassesDirectory);
    }

    private void scanDirectory(final File directory) throws MojoExecutionException {
        getLog().info("Scanning rulesDirectory: " + directory.getAbsolutePath());
        super.executeInTransaction(new StoreOperation<Void, MojoExecutionException>() {
            @Override
            public Void run(Store store) throws MojoExecutionException {
                Artifact artifact = project.getArtifact();
                ArtifactDescriptor descriptor = store.findArtifactDescriptor(artifact.getId());
                if (descriptor == null) {
                    descriptor = store.createArtifactDescriptor(artifact.getId());
                    descriptor.setGroup(artifact.getGroupId());
                    descriptor.setName(artifact.getArtifactId());
                    descriptor.setVersion(artifact.getVersion());
                    descriptor.setClassifier(artifact.getClassifier());
                    descriptor.setType(artifact.getType());
                }
                ClassScannerImpl scanner = new ClassScannerImpl(store);
                try {
                    scanner.scanDirectory(descriptor, directory);
                } catch (IOException e) {
                    throw new MojoExecutionException("Cannot scan classes in " + directory, e);
                }
                return null;
            }
        });
    }
}