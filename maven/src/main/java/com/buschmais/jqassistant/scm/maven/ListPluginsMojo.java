package com.buschmais.jqassistant.scm.maven;

import java.util.List;
import java.util.stream.Collectors;

import com.buschmais.jqassistant.core.plugin.api.PluginInfo;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Lists all plugins known based on the current configuration
 * to jQAssistant.
 */
@Mojo(name = "list-plugins", threadSafe = true, configurator = "custom")
public class ListPluginsMojo extends AbstractProjectMojo {

    @Override
    protected boolean isResetStoreBeforeExecution() {
        return false;
    }

    @Override
    protected boolean isConnectorRequired() {
        return false;
    }

    @Override
    protected void aggregate(MavenProject rootModule, List<MavenProject> projects, Store store, MavenConfiguration configuration)
        throws MojoExecutionException {
        getLog().info("Available plugins for '" + rootModule.getName() + "'.");

        List<PluginInfo> sortedInfos = getPluginRepository(configuration).getPluginOverview()
            .stream()
            .sorted(PluginInfo.NAME_COMPARATOR)
            .collect(Collectors.toList());

        sortedInfos.forEach(info -> {
            CharSequence name = info.getName();
            CharSequence id = info.getId();
            String output = String.format("%s (%s)", name, id);
            System.out.println(output);
        });
    }
}
