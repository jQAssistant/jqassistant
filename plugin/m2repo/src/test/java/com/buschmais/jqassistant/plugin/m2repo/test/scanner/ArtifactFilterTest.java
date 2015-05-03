package com.buschmais.jqassistant.plugin.m2repo.test.scanner;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.Test;

import com.buschmais.jqassistant.plugin.m2repo.impl.scanner.ArtifactFilter;

public class ArtifactFilterTest {

    @Test
    public void includes() {
        ArtifactFilter artifactFilter = new ArtifactFilter(asList("com.buschmais.jqassistant.*:*:jar"), null);
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "jar", null)),
                equalTo(true));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "zip", null)),
                equalTo(false));
    }

    @Test
    public void includesClassifier() {
        ArtifactFilter artifactFilter = new ArtifactFilter(asList("com.buschmais.jqassistant.*:*:jar:sources"), null);
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "jar", "sources")), equalTo(true));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "zip", "tests")), equalTo(false));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "zip", null)), equalTo(false));
    }

    @Test
    public void excludes() {
        ArtifactFilter artifactFilter = new ArtifactFilter(null, asList("com.buschmais.jqassistant.*:*:jar"));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "jar", null)),
                equalTo(false));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "zip", null)),
                equalTo(true));
    }

    @Test
    public void excludesClassifier() {
        ArtifactFilter artifactFilter = new ArtifactFilter(null, asList("com.buschmais.jqassistant.*:*:jar:sources"));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "jar", "sources")),
                equalTo(false));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "zip", "tests")), equalTo(true));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "zip", null)), equalTo(true));
    }

    @Test
    public void includesAndExcludes() {
        ArtifactFilter artifactFilter =
                new ArtifactFilter(asList("com.buschmais.jqassistant.*:*:jar"), asList("com.buschmais.jqassistant.*:*:zip"));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "jar", null)),
                equalTo(true));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "zip", null)),
                equalTo(false));
    }

    @Test
    public void noFilter() {
        ArtifactFilter artifactFilter = new ArtifactFilter(null, null);
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "jar", null)),
                equalTo(true));
        assertThat(artifactFilter.match(getArtifact("com.buschmais.jqassistant.plugin", "jqassistant.plugin.m2repo", "1.0.0", "zip", null)),
                equalTo(true));
    }

    private Artifact getArtifact(String groupId, String artifactId, String version, String type, String classifier) {
        return new DefaultArtifact(groupId, artifactId, VersionRange.createFromVersion(version), null, type, classifier,
                new DefaultArtifactHandler());
    }

}
