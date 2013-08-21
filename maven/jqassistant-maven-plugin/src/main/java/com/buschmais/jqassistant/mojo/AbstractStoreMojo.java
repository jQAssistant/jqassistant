package com.buschmais.jqassistant.mojo;

import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;

/**
 * Abstract base class for mojos using the store.
 */
public abstract class AbstractStoreMojo extends org.apache.maven.plugin.AbstractMojo {

    protected static interface StoreOperation<T, E extends AbstractMojoExecutionException> {
        public T run(Store store) throws E;
    }

    /**
     * The artifactId.
     *
     * @parameter expression="${project.artifactId}"
     * @readonly
     */
    protected String artifactId;


    /**
     * The project rulesDirectory.
     *
     * @parameter expression="${basedir}"
     * @readonly
     */
    protected File basedir;

    /**
     * The build rulesDirectory.
     *
     * @parameter expression="${project.build.rulesDirectory}"
     * @readonly
     */
    protected File buildDirectory;

    /**
     * The classes rulesDirectory.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @readonly
     */
    protected File classesDirectory;

    /**
     * The classes rulesDirectory.
     *
     * @parameter expression="${project.build.testOutputDirectory}"
     * @readonly
     */
    protected File testClassesDirectory;

    /**
     * The store directory.
     *
     * @parameter expression="${jqassistant.store.directory}" default-value="${project.build.directory}/jqassistant/store"
     * @readonly
     */
    protected File storeDirectory;

    /**
     * The Maven project.
     *
     * @parameter expression="${project}"
     */
    protected MavenProject project;

    /**
     * The Maven Session Object
     *
     * @parameter expression="${session}"
     * @readonly
     */
    protected MavenSession session;

    /**
     * Contains the full list of projects in the reactor.
     *
     * @parameter expression = "${reactorProjects}"
     */
    protected List<MavenProject> reactorProjects;

    /**
     * @component
     */
    protected StoreProvider storeProvider;

    protected <T, E extends AbstractMojoExecutionException> T executeInTransaction(StoreOperation<T, E> operation) throws E {
        final Store store = getStore();
        store.beginTransaction();
        try {
            return operation.run(store);
        } finally {
            store.commitTransaction();
        }
    }

    protected <T, E extends AbstractMojoExecutionException> T execute(StoreOperation<T, E> operation) throws E {
        return operation.run(getStore());
    }

    private Store getStore() {
        storeDirectory.getParentFile().mkdirs();
        return storeProvider.getStore(storeDirectory);
    }

}
