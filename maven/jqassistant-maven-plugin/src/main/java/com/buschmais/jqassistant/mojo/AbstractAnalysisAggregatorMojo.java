package com.buschmais.jqassistant.mojo;

import java.util.*;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.DescriptorMapper;

/**
 * Abstract base implementation for analysis mojos running as aggregator.
 */
public abstract class AbstractAnalysisAggregatorMojo extends AbstractAnalysisMojo {

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		MavenProject lastProject = reactorProjects.get(reactorProjects.size() - 1);
		if (currentProject.equals(lastProject)) {
			Map<MavenProject, Set<MavenProject>> baseProjects = new HashMap<>();
			for (MavenProject reactorProject : reactorProjects) {
				MavenProject baseProject = BaseProjectResolver.getBaseProject(reactorProject);
				Set<MavenProject> projects = baseProjects.get(baseProject);
				if (projects == null) {
					projects = new HashSet<>();
					baseProjects.put(baseProject, projects);
				}
				projects.add(reactorProject);
			}

			List<DescriptorMapper<?>> descriptorMappers;
			try {
				descriptorMappers = pluginManager.getDescriptorMappers();
			} catch (PluginReaderException e) {
				throw new MojoExecutionException("Cannot get descriptor mappers.", e);
			}

			for (Map.Entry<MavenProject, Set<MavenProject>> entry : baseProjects.entrySet()) {
				MavenProject baseProject = entry.getKey();
				Store store = getStore(baseProject);
				try {
					store.start(descriptorMappers);
					Set<MavenProject> projects = entry.getValue();
					aggregate(baseProject, projects, store);
				} finally {
					store.stop();
				}
			}
		}
	}

	/**
	 * Execute the aggregated analysis.
	 * 
	 * @throws MojoExecutionException
	 *             If execution fails.
	 * @throws MojoFailureException
	 *             If execution fails.
	 */
	protected abstract void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException,
			MojoFailureException;
}
