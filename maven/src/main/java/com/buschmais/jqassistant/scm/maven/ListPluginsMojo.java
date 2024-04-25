package com.buschmais.jqassistant.scm.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Lists all plugins known based on the current configuration
 * to jQAssistant.
 */
@Mojo(name = "list-plugins", threadSafe = true)
public class ListPluginsMojo extends AbstractProjectMojo {

    @Override
    protected void aggregate(MojoExecutionContext mojoExecutionContext) throws MojoExecutionException {
        // Nothing to do here, plugins are listed on startup
    }
}
