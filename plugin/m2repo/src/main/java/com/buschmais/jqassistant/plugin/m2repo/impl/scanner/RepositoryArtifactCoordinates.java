package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import com.buschmais.jqassistant.plugin.common.api.scanner.artifact.Coordinates;
import org.eclipse.aether.artifact.Artifact;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RepositoryArtifactCoordinates implements Coordinates {

    private static final String DATEFORMAT_TIMESTAMP_SNAPSHOT = "yyyyMMddHHmmss";

    private Artifact artifact;

    private long lastModified;

    public RepositoryArtifactCoordinates(Artifact artifact, long lastModified) {
        this.artifact = artifact;
        this.lastModified = lastModified;
    }

    @Override
    public String getGroup() {
        return artifact.getGroupId();
    }

    @Override
    public String getName() {
        return artifact.getArtifactId();
    }

    @Override
    public String getClassifier() {
        return artifact.getClassifier();
    }

    @Override
    public String getType() {
        return artifact.getExtension();
    }

    @Override
    public String getVersion() {
        if (artifact.isSnapshot()) {
            String timeStamp = new SimpleDateFormat(DATEFORMAT_TIMESTAMP_SNAPSHOT).format(new Date(lastModified));
            return artifact.getBaseVersion() + "-" + timeStamp;
        } else {
            return artifact.getVersion();
        }
    }
}
