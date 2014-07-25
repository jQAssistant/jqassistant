package com.buschmais.jqassistant.scm.maven;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Created by dimahler on 7/25/2014.
 */
public abstract class AbstractModuleMojo extends AbstractMojo {

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        List<Class<?>> descriptorTypes;
        MavenProject baseProject = BaseProjectResolver.getBaseProject(currentProject);
        Store store = getStore(baseProject, currentProject == currentProject.getExecutionProject());
        try {
            descriptorTypes = getScannerPluginRepository(store, getPluginProperties(currentProject)).getDescriptorTypes();
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot get descriptor mappers.", e);
        }
        try {
            store.start(descriptorTypes);
            execute(currentProject, store);
        } finally {
            store.stop();
        }
    }

    protected abstract void execute(MavenProject mavenProject, Store store) throws MojoExecutionException, MojoFailureException;

}
