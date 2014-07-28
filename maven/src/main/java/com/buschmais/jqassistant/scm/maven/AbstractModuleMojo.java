package com.buschmais.jqassistant.scm.maven;

import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Abstract base class for mojos which are executed per module.
 */
public abstract class AbstractModuleMojo extends AbstractMojo {

    @Override
    public final void doExecute() throws MojoExecutionException, MojoFailureException {
        List<Class<?>> descriptorTypes;
        MavenProject baseProject = ProjectResolver.getRootModule(currentProject);
        Store store = getStore(baseProject, currentProject == currentProject.getExecutionProject());
        try {
            descriptorTypes = pluginRepositoryProvider.getScannerPluginRepository(store, getPluginProperties(currentProject)).getDescriptorTypes();
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
