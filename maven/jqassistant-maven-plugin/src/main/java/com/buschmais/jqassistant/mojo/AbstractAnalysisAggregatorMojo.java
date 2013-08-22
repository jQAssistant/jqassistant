package com.buschmais.jqassistant.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract base implementation for analysis mojos running as aggregator.
 */
public abstract class AbstractAnalysisAggregatorMojo extends AbstractAnalysisMojo {

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        MavenProject lastProject = reactorProjects.get(reactorProjects.size() - 1);
        if (project.equals(lastProject)) {
            aggregate();
        }
    }

    /**
     * Execute the aggregated analysis.
     *
     * @throws MojoExecutionException If execution fails.
     * @throws MojoFailureException   If execution fails.
     */
    protected abstract void aggregate() throws MojoExecutionException, MojoFailureException;
}
