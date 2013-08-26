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

    /**
     * The artifact type for test jars.
     */
    public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // reset the store if the current project is the base project (i.e. where the rules are located).
        if (project != null && project.equals(getBaseProject())) {
            reset();
        }
        scanDirectory(classesDirectory, false);
        scanDirectory(testClassesDirectory, true);
    }

    /**
     * Reset the store.
     */
    private void reset() throws MojoFailureException, MojoExecutionException {
        executeInTransaction(new StoreOperation<Void>() {
            @Override
            public Void run(Store store) throws MojoExecutionException, MojoFailureException {
                store.reset();
                return null;
            }
        });
    }

    /**
     * Scan the given directory for classes.
     *
     * @param directory The directory.
     * @throws MojoExecutionException If scanning fails.
     */
    private void scanDirectory(final File directory, final boolean testJar) throws MojoExecutionException, MojoFailureException {
        getLog().info("Scanning classes directory: " + directory.getAbsolutePath());
        super.executeInTransaction(new StoreOperation<Void>() {
            @Override
            public Void run(Store store) throws MojoExecutionException {
                Artifact artifact = project.getArtifact();
                String type = testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType();
                String id = createArtifactDescriptorId(artifact.getGroupId(), artifact.getArtifactId(), type, artifact.getClassifier(), artifact.getVersion());
                ArtifactDescriptor descriptor = store.find(ArtifactDescriptor.class, id);
                if (descriptor == null) {
                    descriptor = store.create(ArtifactDescriptor.class, id);
                    descriptor.setGroup(artifact.getGroupId());
                    descriptor.setName(artifact.getArtifactId());
                    descriptor.setVersion(artifact.getVersion());
                    descriptor.setClassifier(artifact.getClassifier());
                    descriptor.setType(type);
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

    /**
     * Creates the id of an artifact descriptor by the given items.
     *
     * @param group      The group.
     * @param name       The name.
     * @param type       The type.
     * @param classifier The classifier (optional).
     * @param version    The version.
     * @return The id.
     */
    private String createArtifactDescriptorId(String group, String name, String type, String classifier, String version) {
        StringBuffer id = new StringBuffer();
        id.append(group);
        id.append(':');
        id.append(name);
        id.append(':');
        id.append(type);
        if (classifier != null) {
            id.append(':');
            id.append(classifier);
        }
        id.append(':');
        id.append(version);
        return id.toString();
    }
}