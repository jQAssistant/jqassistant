package com.buschmais.jqassistant.scm.maven;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.store.api.Store;

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

    /**
     * The store repository.
     */
    @Component
    private StoreRepository storeRepository;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        Aggregator.execute(new Aggregator.AggregatedGoal() {
            public void execute(MavenProject baseProject, Set<MavenProject> projects) throws MojoExecutionException, MojoFailureException {
                List<Class<?>> descriptorTypes;
                Store store = getStore(baseProject);
                try {
                    descriptorTypes = getScannerPluginRepository(store, new Properties()).getDescriptorTypes();
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
     * Return the store instance to use for the given base project.
     * 
     * @param baseProject
     *            The base project
     * @return The store instance.
     * @throws MojoExecutionException
     *             If the store cannot be created.
     */
    protected Store getStore(MavenProject baseProject) throws MojoExecutionException {
        File directory;
        if (this.storeDirectory != null) {
            directory = this.storeDirectory;
        } else {
            directory = new File(baseProject.getBuild().getDirectory() + "/jqassistant/store");
        }
        return storeRepository.getStore(directory, isResetStoreOnInitialization());
    }

    /**
     * Determine if a goal needs to reset the store on initialization.
     * 
     * @return <code>true</code> If the store shall be reset initially.
     */
    protected abstract boolean isResetStoreOnInitialization();

    /**
     * Execute the aggregated analysis.
     * 
     * @throws MojoExecutionException
     *             If execution fails.
     * @throws MojoFailureException
     *             If execution fails.
     */
    protected abstract void aggregate(MavenProject baseProject, Set<MavenProject> projects, Store store) throws MojoExecutionException, MojoFailureException;
}
