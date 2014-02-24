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
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ProjectScanner;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.FileScannerImpl;
import com.buschmais.jqassistant.core.scanner.impl.ProjectScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.ArtifactDescriptor;

/**
 * Scans the the output directory and test output directory.
 */
@Mojo(name = "scan", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class ScanMojo extends AbstractAnalysisAggregatorMojo {

    /**
     * The artifact type for test jars.
     */
    public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

	@Override
	protected void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException,
			MojoFailureException {
		// reset the store if the current project is the base project (i.e. where the rules are located).
		store.reset();
		for (MavenProject project : projects) {
			List<FileScannerPlugin<?>> fileScannerPlugins;
			List<ProjectScannerPlugin<?>> projectScannerPlugins;
			try {
				fileScannerPlugins = pluginManager.getScannerPlugins(store, new Properties());
				projectScannerPlugins = pluginManager.getProjectScannerPlugins();
			} catch (PluginReaderException e) {
				throw new MojoExecutionException("Cannot get scanner plugins.", e);
			}
			scanProject(baseProject, project, store, project.getBasedir(), projectScannerPlugins, fileScannerPlugins);
			scanDirectory(baseProject, project, store, project.getBuild().getOutputDirectory(), false, fileScannerPlugins);
			scanDirectory(baseProject, project, store, project.getBuild().getTestOutputDirectory(), true, fileScannerPlugins);
            scanTestReports(project.getBuild().getDirectory() + "/surefire-reports", store, fileScannerPlugins);
            scanTestReports(project.getBuild().getDirectory() + "/failsafe-reports", store, fileScannerPlugins);
		}
	}

	private void scanProject(MavenProject baseProject, MavenProject project, Store store, File basedir,
			List<ProjectScannerPlugin<?>> projectScannerPlugins, List<FileScannerPlugin<?>> fileScannerPlugins)
			throws MojoExecutionException {

		store.beginTransaction();
		try {
			ArtifactDescriptor artifactDescriptor = getStoredArtifact(project, store, false);
			ProjectScanner projectScanner = new ProjectScannerImpl(store, projectScannerPlugins);
			FileScanner fileScanner = new FileScannerImpl(fileScannerPlugins);
			try {
				List<File> additionalFiles = projectScanner.getAdditionalFiles(project);
				for (Descriptor descriptor : fileScanner.scanFiles(baseProject.getBasedir(), additionalFiles)) {
					artifactDescriptor.getContains().add(descriptor);
				}

			} catch (IOException e) {
				throw new MojoExecutionException("Cannot scan files", e);
			}
		} finally {
			store.commitTransaction();
		}
	}

	/**
	 * Scan the given directory for classes.
	 * 
	 * @param directoryName
	 *            The directory.
	 * @throws MojoExecutionException
	 *             If scanning fails.
	 */
	private void scanDirectory(MavenProject baseProject, final MavenProject project, Store store, final String directoryName,
			boolean testJar, final List<FileScannerPlugin<?>> scannerPlugins) throws MojoExecutionException, MojoFailureException {
		final File directory = new File(directoryName);

		if (!directory.exists()) {
			getLog().info("Directory '" + directory.getAbsolutePath() + "' does not exist, skipping scan.");
		} else {
			store.beginTransaction();
			try {
				ArtifactDescriptor artifactDescriptor = getStoredArtifact(project, store, testJar);
				FileScanner scanner = new FileScannerImpl(scannerPlugins);
				try {
					for (Descriptor descriptor : scanner.scanDirectory(directory)) {
						artifactDescriptor.getContains().add(descriptor);
					}
				} catch (IOException e) {
					throw new MojoExecutionException("Cannot scan directory '" + directory.getAbsolutePath() + "'", e);
				}
			} finally {
				store.commitTransaction();
			}
		}
	}

	private ArtifactDescriptor getStoredArtifact(final MavenProject project, Store store, boolean testJar) {
		Artifact artifact = project.getArtifact();
		String type = testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType();
		String id = createArtifactDescriptorId(artifact.getGroupId(), artifact.getArtifactId(), type, artifact.getClassifier(),
				artifact.getVersion());
		ArtifactDescriptor artifactDescriptor = store.find(ArtifactDescriptor.class, id);
		if (artifactDescriptor == null) {
			artifactDescriptor = store.create(ArtifactDescriptor.class, id);
			artifactDescriptor.setGroup(artifact.getGroupId());
			artifactDescriptor.setName(artifact.getArtifactId());
			artifactDescriptor.setVersion(artifact.getVersion());
			artifactDescriptor.setClassifier(artifact.getClassifier());
			artifactDescriptor.setType(type);
		}
		return artifactDescriptor;
	}

    /**
     * Scans a directory for test reports.
     *
     * @param directoryName  The directory name.
     * @param store          The store.
     * @param scannerPlugins The plugins.
     * @throws MojoExecutionException If scanning fails.
     */
    private void scanTestReports(String directoryName, Store store, List<FileScannerPlugin<?>> scannerPlugins) throws MojoExecutionException {
        FileScanner scanner = new FileScannerImpl(scannerPlugins);
        final File directory = new File(directoryName);
        if (directory.exists()) {
            store.beginTransaction();
            try {
                for (Descriptor descriptor : scanner.scanDirectory(directory, false)) {
                }
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot scan directory '" + directory.getAbsolutePath() + "'", e);
            } finally {
                store.commitTransaction();
            }
        }
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
