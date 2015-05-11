package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import org.apache.maven.model.Plugin;

import com.buschmais.jqassistant.plugin.common.api.scanner.artifact.Coordinates;

/**
 * Created by dimahler on 5/5/2015.
 */
public final class PluginCoordinates implements Coordinates {

    private Plugin plugin;

    public PluginCoordinates(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getGroup() {
        return plugin.getGroupId();
    }

    @Override
    public String getName() {
        return plugin.getArtifactId();
    }

    @Override
    public String getClassifier() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getVersion() {
        return plugin.getVersion();
    }

}
