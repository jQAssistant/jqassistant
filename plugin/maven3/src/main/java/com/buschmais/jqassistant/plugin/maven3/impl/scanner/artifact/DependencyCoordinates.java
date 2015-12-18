package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import org.apache.maven.model.Dependency;

import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;

/**
 * Created by dimahler on 5/5/2015.
 */
public class DependencyCoordinates implements Coordinates {

    private Dependency dependency;

    public DependencyCoordinates(Dependency dependency) {
        this.dependency = dependency;
    }

    @Override
    public String getGroup() {
        return dependency.getGroupId();
    }

    @Override
    public String getName() {
        return dependency.getArtifactId();
    }

    @Override
    public String getClassifier() {
        return dependency.getClassifier();
    }

    @Override
    public String getType() {
        return dependency.getType();
    }

    @Override
    public String getVersion() {
        return dependency.getVersion();
    }

}
