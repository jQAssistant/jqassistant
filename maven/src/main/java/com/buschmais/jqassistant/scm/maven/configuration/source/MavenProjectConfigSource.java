package com.buschmais.jqassistant.scm.maven.configuration.source;

import org.apache.maven.project.MavenProject;

import static java.util.Arrays.asList;

/**
 * Config source for a {@link MavenProject}.
 */
public class MavenProjectConfigSource extends AbstractObjectValueConfigSource<MavenProject> {

    public MavenProjectConfigSource(MavenProject mavenProject) {
        super("Maven Project", mavenProject, "project",
            asList("project.name", "project.description", "project.groupId", "project.artifactId", "project.version", "project.packaging", "project.basedir",
                "project.build.sourceDirectory", "project.build.scriptSourceDirectory", "project.build.testSourceDirectory", "project.build.directory",
                "project.build.outputDirectory", "project.build.testOutputDirectory", "project.build.sourceEncoding", "project.build.finalName",
                "project.reporting.outputEncoding"));
    }

}
