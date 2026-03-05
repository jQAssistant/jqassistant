package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.ArtifactScopedTypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenMainArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;

import org.apache.maven.project.MavenProject;

/**
 * Scanner for Maven Web Projects (packaging "war").
 */
@Requires(MavenProjectDirectoryDescriptor.class)
public class MavenWARProjectScannerPlugin extends AbstractScannerPlugin<MavenProject, MavenProjectDirectoryDescriptor> {

    public static final String WEBAPP_DIR = "src/main/webapp";

    @Override
    public boolean accepts(MavenProject item, String path, Scope scope) {
        return "war".equalsIgnoreCase(item.getPackaging());
    }

    @Override
    public MavenProjectDirectoryDescriptor scan(MavenProject item, String path, Scope scope, Scanner scanner) throws IOException {
        File basedir = item.getBasedir();
        MavenProjectDirectoryDescriptor projectDirectoryDescriptor = scanner.getContext()
            .getCurrentDescriptor();
        File webapp = new File(basedir, WEBAPP_DIR);
        scanWebApp(scope, scanner, webapp, projectDirectoryDescriptor);
        return projectDirectoryDescriptor;
    }

    private static void scanWebApp(Scope scope, Scanner scanner, File webapp, MavenProjectDirectoryDescriptor projectDirectoryDescriptor) {
        if (webapp.exists() && webapp.isDirectory()) {
            List<ArtifactDescriptor> createsArtifacts = projectDirectoryDescriptor.getCreatesArtifacts();
            Optional<JavaArtifactFileDescriptor> mainArtifactDescriptor = createsArtifacts.stream()
                .filter(
                    artifactDescriptor -> artifactDescriptor instanceof MavenMainArtifactDescriptor && artifactDescriptor instanceof JavaArtifactFileDescriptor)
                .map(a -> (JavaArtifactFileDescriptor) a)
                .findFirst();
            mainArtifactDescriptor.ifPresent(artifactDescriptor -> scanner.getContext()
                .push(TypeResolver.class, new ArtifactScopedTypeResolver(artifactDescriptor)));
            try {
                FileDescriptor descriptor = scanner.scan(webapp, WEBAPP_DIR, scope);
                projectDirectoryDescriptor.getContains()
                    .add(descriptor);
            } finally {
                mainArtifactDescriptor.ifPresent(artifactDescriptor -> scanner.getContext()
                    .pop(TypeResolver.class));
            }
        }
    }
}
