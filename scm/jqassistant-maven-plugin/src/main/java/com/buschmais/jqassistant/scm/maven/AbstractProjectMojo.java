package com.buschmais.jqassistant.scm.maven;

import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Abstract base implementation for analysis mojos.
 */
public abstract class AbstractProjectMojo extends AbstractMojo {

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        Aggregator.execute(new Aggregator.AggregatedGoal() {
            public void execute(MavenProject baseProject, Set<MavenProject> projects) throws MojoExecutionException, MojoFailureException {
                List<Class<?>> descriptorTypes;
                Store store = getStore(baseProject);
                try {
                    descriptorTypes = getScannerPluginRepository(store, getPluginProperties(baseProject)).getDescriptorTypes();
                } catch (PluginRepositoryException e) {
                    throw new MojoExecutionException("Cannot get descriptor mappers.", e);
                }
                try {
                    store.start(descriptorTypes);
                    AbstractProjectMojo.this.aggregate(baseProject, projects, store);
                } finally {
                    store.stop();
                }
            }
        }, currentProject, reactorProjects);
    }

    /**
     * Execute the aggregated analysis.
     *
     * @throws org.apache.maven.plugin.MojoExecutionException
     *             If execution fails.
     * @throws org.apache.maven.plugin.MojoFailureException
     *             If execution fails.
     */
    protected abstract void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException;

}
