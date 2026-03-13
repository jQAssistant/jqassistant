package com.buschmais.jqassistant.plugin.maven3.impl.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenProjectDirectoryDescriptor;

import org.apache.maven.project.MavenProject;

/**
 * Scanner for Maven Web Projects (packaging "war").
 */
@Requires(MavenProjectDirectoryDescriptor.class)
public class MavenEARProjectScannerPlugin extends AbstractScannerPlugin<MavenProject, MavenProjectDirectoryDescriptor> {

    public static final String APPLICATION_DIR = "src/main/application";

    @Override
    public boolean accepts(MavenProject item, String path, Scope scope) {
        return "ear".equalsIgnoreCase(item.getPackaging());
    }

    @Override
    public MavenProjectDirectoryDescriptor scan(MavenProject item, String path, Scope scope, Scanner scanner) throws IOException {
        File basedir = item.getBasedir();
        MavenProjectDirectoryDescriptor projectDirectoryDescriptor = scanner.getContext()
            .getCurrentDescriptor();
        File application = new File(basedir, APPLICATION_DIR);
        scanApplication(scope, scanner, application, projectDirectoryDescriptor);
        return projectDirectoryDescriptor;
    }

    private static void scanApplication(Scope scope, Scanner scanner, File application, MavenProjectDirectoryDescriptor projectDirectoryDescriptor) {
        if (application.exists() && application.isDirectory()) {
            FileDescriptor descriptor = scanner.scan(application, APPLICATION_DIR, scope);
            projectDirectoryDescriptor.getContains()
                .add(descriptor);
        }
    }
}
