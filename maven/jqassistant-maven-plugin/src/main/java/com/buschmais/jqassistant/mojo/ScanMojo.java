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
import com.buschmais.jqassistant.core.scanner.api.ArtifactScanner;
import com.buschmais.jqassistant.core.scanner.impl.ArtifactScannerImpl;
import com.buschmais.jqassistant.core.scanner.impl.ClassScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;

/**
 * A Mojo which scans the compiled classes in the output directory and test output directory.
 *
 * @phase package
 * @goal scan
 */
public class ScanMojo extends AbstractAnalysisMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        scanDirectory(classesDirectory);
        scanDirectory(testClassesDirectory);
    }

    /**
     * Scan the given directory for classes.
     *
     * @param directory The directory.
     * @throws MojoExecutionException If scanning fails.
     */
    private void scanDirectory(final File directory) throws MojoExecutionException, MojoFailureException {
        getLog().info("Scanning classes directory: " + directory.getAbsolutePath());
        super.executeInTransaction(new StoreOperation<Void>() {
            @Override
            public Void run(Store store) throws MojoExecutionException {
                Artifact artifact = project.getArtifact();
                ArtifactDescriptor descriptor = store.find(ArtifactDescriptor.class, artifact.getId());
                if (descriptor == null) {
                    descriptor = store.create(ArtifactDescriptor.class, artifact.getId());
                    descriptor.setGroup(artifact.getGroupId());
                    descriptor.setName(artifact.getArtifactId());
                    descriptor.setVersion(artifact.getVersion());
                    descriptor.setClassifier(artifact.getClassifier());
                    descriptor.setType(artifact.getType());
                }
                ArtifactScanner scanner = new ArtifactScannerImpl(new ClassScannerImpl(store));
                try {
                    scanner.scanClassDirectory(descriptor, directory);
                } catch (IOException e) {
                    throw new MojoExecutionException("Cannot scan classes in " + directory, e);
                }
                return null;
            }
        });
    }
}