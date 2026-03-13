package com.buschmais.jqassistant.scm.maven;

import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Defines the interface of a jQAssistant Maven task
 */
public interface MavenTask {

    /**
     * Execute the task
     *
     * @param configuration
     *     The {@link MavenConfiguration}
     * @param mavenTaskContext
     *     The {@link MavenTaskContext}
     * @throws MojoExecutionException
     *     If the task cannot be executed.
     * @throws MojoFailureException
     *     If the task failed.
     */
    void execute(MavenConfiguration configuration, MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException;

    /**
     * Prepare a project.
     *
     * @param mavenTaskContext
     *     The {@link MavenTaskContext}
     * @throws MojoExecutionException
     *     If the task cannot be executed.
     * @throws MojoFailureException
     *     If the task failed.
     */
    default void prepareProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
    }

    /**
     * Enter a project.
     *
     * @param mavenTaskContext
     *     The {@link MavenTaskContext}
     * @throws MojoExecutionException
     *     If the task cannot be executed.
     * @throws MojoFailureException
     *     If the task failed.
     */
    default void enterProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
    }

    /**
     * Enter a module of a project.
     */
    default void enterModule(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
    }

    /**
     * Leave a project.
     *
     * @param mavenTaskContext
     *     The {@link MavenTaskContext}
     * @throws MojoExecutionException
     *     If the task cannot be executed.
     * @throws MojoFailureException
     *     If the task failed.
     */
    default void leaveProject(MavenTaskContext mavenTaskContext) throws MojoExecutionException, MojoFailureException {
    }

}
