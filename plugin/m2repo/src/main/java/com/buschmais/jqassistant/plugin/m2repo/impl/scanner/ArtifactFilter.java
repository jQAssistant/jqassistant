package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;

import java.util.List;

/**
 * A configurable artifact filter which handles includes and excludes patterns.
 *
 * Supported patterns:
 * <ul>
 * <li>[groupId]:[artifactId]:[type]:[version]</li>
 * <li>[groupId]:[artifactId]:[type]:[classifier]:[version]</li>
 * </ul>
 */
public class ArtifactFilter {

    private PatternIncludesArtifactFilter includesFilter;
    private PatternExcludesArtifactFilter excludesFilter;

    /**
     * Constructor.
     * 
     * @param includes
     *            The list of include patterns or <code>null</code> to include everything.
     * @param excludes
     *            The list of exclude patterns or <code>null</code> to exclude nothing.
     */
    public ArtifactFilter(List<String> includes, List<String> excludes) {
        includesFilter = includes != null ? new PatternIncludesArtifactFilter(includes) : null;
        excludesFilter = excludes != null ? new PatternExcludesArtifactFilter(excludes) : null;
    }

    /**
     * Matches the given artifact against the filter configuration.
     * 
     * @param artifact
     *            The artifact.
     * @return <code>true</code> if the artifact matches the filter.
     */
    public boolean match(Artifact artifact) {
        return (includesFilter == null || includesFilter.include(artifact)) && (excludesFilter == null || excludesFilter.include(artifact));
    }

    @Override
    public String toString() {
        return "ArtifactFilter{" + "includesFilter=" + includesFilter + ", excludesFilter=" + excludesFilter + '}';
    }
}
