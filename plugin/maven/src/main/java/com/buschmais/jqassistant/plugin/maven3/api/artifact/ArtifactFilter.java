package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;

/**
 * A configurable artifact filter which handles includes and excludes patterns.
 *
 * Supported patterns:
 *
 * - `[groupId]:[artifactId]:[type]:[version]` -
 * `[groupId]:[artifactId]:[type]:[classifier]:[version]`
 */
public class ArtifactFilter {

    private final List<String> includes;
    private final List<String> excludes;

    /**
     * Constructor.
     *
     * @param includes
     *            The list of include patterns or `null` to include everything.
     * @param excludes
     *            The list of exclude patterns or `null` to exclude nothing.
     */
    public ArtifactFilter(String includes, String excludes) {
        this.includes = parseFilterPattern(includes);
        this.excludes = parseFilterPattern(excludes);
    }

    /**
     * Matches the given artifact against the filter configuration.
     *
     * @param artifact
     *            The artifact.
     * @return `true` if the artifact matches the filter.
     */
    public boolean match(Artifact artifact) {
        PatternIncludesArtifactFilter includesFilter = includes != null ? new PatternIncludesArtifactFilter(includes) : null;
        PatternExcludesArtifactFilter excludesFilter = excludes != null ? new PatternExcludesArtifactFilter(excludes) : null;
        return (includesFilter == null || includesFilter.include(artifact)) && (excludesFilter == null || excludesFilter.include(artifact));
    }

    private List<String> parseFilterPattern(String patterns) {
        if (patterns == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (String pattern : patterns.split(",")) {
            String trimmed = pattern.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "ArtifactFilter{" + "includes=" + includes + ", excludes=" + excludes + '}';
    }
}
