package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.buschmais.jqassistant.mojo.Aggregator.AggregatedGoal;

/**
 * Abstract base implementation for analysis mojos running as aggregator.
 */
public abstract class AbstractAnalysisAggregatorMojo extends AbstractAnalysisMojo {

	/**
	 * Contains the full list of projects in the reactor.
	 */
	@Parameter(property = "reactorProjects")
	private List<MavenProject> reactorProjects;

	/**
	 * The Maven project.
	 */
	@Parameter(property = "project")
	private MavenProject currentProject;

	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		Aggregator.execute(new AggregatedGoal() {
			public void execute(MavenProject baseProject, Set<MavenProject> projects) throws MojoExecutionException, MojoFailureException {
				List<Class<?>> descriptorTypes;
                Store store = getStore(baseProject);
				try {
					descriptorTypes = getScannerPluginManager(store, new Properties()).getDescriptorTypes();
				} catch (PluginReaderException e) {
					throw new MojoExecutionException("Cannot get descriptor mappers.", e);
				}
				try {
					store.start(descriptorTypes);
					AbstractAnalysisAggregatorMojo.this.aggregate(baseProject, projects, store);
				} finally {
					store.stop();
				}
			}
		}, currentProject, reactorProjects);
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
